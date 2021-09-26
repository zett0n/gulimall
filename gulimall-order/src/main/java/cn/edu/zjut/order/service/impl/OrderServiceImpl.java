package cn.edu.zjut.order.service.impl;

import cn.edu.zjut.common.dto.SkuHasStockDTO;
import cn.edu.zjut.common.exception.NoStockException;
import cn.edu.zjut.common.utils.PageUtils;
import cn.edu.zjut.common.utils.Query;
import cn.edu.zjut.common.utils.R;
import cn.edu.zjut.common.vo.*;
import cn.edu.zjut.order.dao.OrderDao;
import cn.edu.zjut.order.dto.OrderCreateDTO;
import cn.edu.zjut.order.dto.SpuInfoDTO;
import cn.edu.zjut.order.entity.OrderEntity;
import cn.edu.zjut.order.entity.OrderItemEntity;
import cn.edu.zjut.order.enume.OrderStatusEnum;
import cn.edu.zjut.order.feign.CartFeignService;
import cn.edu.zjut.order.feign.MemberFeignService;
import cn.edu.zjut.order.feign.ProductFeignService;
import cn.edu.zjut.order.feign.WareFeignService;
import cn.edu.zjut.order.interceptor.OrderInterceptor;
import cn.edu.zjut.order.service.OrderItemService;
import cn.edu.zjut.order.service.OrderService;
import cn.edu.zjut.order.vo.OrderConfirmVO;
import cn.edu.zjut.order.vo.OrderSubmitVO;
import cn.edu.zjut.order.vo.SubmitOrderResponseVO;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static cn.edu.zjut.common.constant.DefaultConstant.R_SUCCESS_CODE;
import static cn.edu.zjut.common.constant.OrderConstant.TOKEN_TTL;
import static cn.edu.zjut.common.constant.OrderConstant.USER_ORDER_TOKEN_PREFIX;

@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    @Autowired
    private MemberFeignService memberFeignService;

    @Autowired
    private CartFeignService cartFeignService;

    @Autowired
    private ThreadPoolExecutor executor;

    @Autowired
    private WareFeignService wareFeignService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private OrderItemService orderItemService;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(new Query<OrderEntity>().getPage(params), new QueryWrapper<OrderEntity>());

        return new PageUtils(page);
    }


    @Override
    public OrderConfirmVO confirmOrder() throws ExecutionException, InterruptedException {
        OrderConfirmVO orderConfirmVO = new OrderConfirmVO();
        MemberResponseVO memberResponseVO = OrderInterceptor.loginUser.get();

        /*
         * 1、异步远程查询所有的收货地址列表
         */
        CompletableFuture<Void> addressTask = CompletableFuture.runAsync(() -> {
            List<MemberAddressVO> memberAddressVOS = this.memberFeignService.getAddressById(memberResponseVO.getId());
            orderConfirmVO.setMemberAddressVOs(memberAddressVOS);
        }, this.executor);

        /*
         * 2、异步远程查询购物车所有选中购物项
         *
         * 远程查询购物车服务时，由于购物车方法的执行需要通过拦截器获取 session 中的信息
         * 因此需提前配置 feign，使其携带 cookie
         *
         * 异步执行使用了线程池，考虑到 feign 配置中用到了 threadlocal，就需要考虑将主线程的内容同步给线程池中的线程（其他不是购物车的远程调用不需要）
         */
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        CompletableFuture<Void> cartTask = CompletableFuture.runAsync(() -> {
                    RequestContextHolder.setRequestAttributes(requestAttributes);

                    List<OrderItemVO> checkedItems = this.cartFeignService.getCheckedItems();
                    orderConfirmVO.setItems(checkedItems);
                }, this.executor)
                /*
                 * 3、查询到购物项后，查询每个购物项是否有库存，结果封装入 map
                 */
                .thenRunAsync(() -> {
                    List<OrderItemVO> items = orderConfirmVO.getItems();
                    List<Long> skuIds = items.stream().map(OrderItemVO::getSkuId).collect(Collectors.toList());

                    R r = this.wareFeignService.hasStock(skuIds);
                    List<SkuHasStockDTO> skuHasStockDTOs = r.parseObjectFromMap("data", new TypeReference<List<SkuHasStockDTO>>() {
                    });

                    if (skuHasStockDTOs != null) {
                        Map<Long, Boolean> stockMap =
                                skuHasStockDTOs.stream().collect(Collectors.toMap(SkuHasStockDTO::getSkuId, SkuHasStockDTO::getHasStock));

                        orderConfirmVO.setStocks(stockMap);
                    }
                }, this.executor);

        /*
         * 4、查询用户积分
         */
        Integer integration = memberResponseVO.getIntegration();
        orderConfirmVO.setIntegration(integration);

        /*
         * 5、防重令牌
         */
        String token = UUID.randomUUID().toString().replace("-", "");
        orderConfirmVO.setOrderToken(token);
        this.stringRedisTemplate.opsForValue().set(USER_ORDER_TOKEN_PREFIX + memberResponseVO.getId(), token, TOKEN_TTL, TimeUnit.MINUTES);

        CompletableFuture.allOf(addressTask, cartTask).get();
        return orderConfirmVO;
    }


    @Override
    @Transactional
    public SubmitOrderResponseVO submitOrder(OrderSubmitVO orderSubmitVO) {

        SubmitOrderResponseVO submitOrderResponseVO = new SubmitOrderResponseVO();
        MemberResponseVO memberResponseVO = OrderInterceptor.loginUser.get();

        /* ------------------------------------------ A、防重校验令牌 ------------------------------------------ */

        // lua 脚本
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        // key 不存在或者删除失败：0 删除成功：1
        Long execute = this.stringRedisTemplate.execute(new DefaultRedisScript<>(script, Long.class),
                Collections.singletonList(USER_ORDER_TOKEN_PREFIX + memberResponseVO.getId()), orderSubmitVO.getOrderToken());

        // 防重令牌验证失败
        if (execute == null || execute == 0L) {
            submitOrderResponseVO.setCode(1);
            return submitOrderResponseVO;
        }

        /* -------------------------------------------- B、创建订单 -------------------------------------------- */

        OrderCreateDTO orderCreateDTO = createOrder(orderSubmitVO, memberResponseVO);

        /* ---------------------------------------------- C、验价 ---------------------------------------------- */

        BigDecimal payAmount = orderCreateDTO.getOrder().getPayAmount();
        // 前端提交的金钱
        BigDecimal payPrice = orderSubmitVO.getPayPrice();

        // 验价失败
        if (Math.abs(payAmount.subtract(payPrice).doubleValue()) >= 0.01) {
            submitOrderResponseVO.setCode(2);
            return submitOrderResponseVO;
        }

        /* --------------------- D、保存订单（开始修改数据库，之前都只为查询，失败了无需抛异常回滚） --------------------- */

        saveOrder(orderCreateDTO);

        /* --------------------------------------------- E、锁库存 --------------------------------------------- */

        // 封装锁库存信息
        WareSkuLockVO wareSkuLockVO = new WareSkuLockVO();

        List<OrderItemVO> orderItemVOS = orderCreateDTO.getOrderItems().stream().map(item -> {
            OrderItemVO orderItemVO = new OrderItemVO();
            orderItemVO.setSkuId(item.getSkuId())
                    .setCount(item.getSkuQuantity());
            return orderItemVO;
        }).collect(Collectors.toList());

        wareSkuLockVO.setOrderSn(orderCreateDTO.getOrder().getOrderSn())
                .setLocks(orderItemVOS);

        // 远程锁库存
        R r = this.wareFeignService.orderLockStock(wareSkuLockVO);

        // 锁定库存失败，需要抛出异常，回滚订单
        if (r.getCode() != R_SUCCESS_CODE) {
            String msg = (String) r.get("msg");
            throw new NoStockException(msg);
        }

        /* -------------------------------------------- F、下单成功 -------------------------------------------- */
        submitOrderResponseVO.setCode(0)
                .setOrder(orderCreateDTO.getOrder());

        return submitOrderResponseVO;
    }


    /**
     * 创建订单
     */
    private OrderCreateDTO createOrder(OrderSubmitVO orderSubmitVO, MemberResponseVO memberResponseVO) {
        OrderCreateDTO orderCreateDTO = new OrderCreateDTO();

        // 1、使用 MB 的工具类生成订单号
        String orderSn = IdWorker.getTimeId();

        // 2、构建订单号
        OrderEntity orderEntity = buildOrder(orderSubmitVO, memberResponseVO, orderSn);

        // 3、构建所有订单项
        List<OrderItemEntity> orderItemEntities = buildOrderItems(orderSn);

        // 4、后台重新计算价格
        computePrice(orderEntity, orderItemEntities);

        orderCreateDTO.setOrder(orderEntity)
                .setOrderItems(orderItemEntities);

        return orderCreateDTO;
    }


    /**
     * 构建订单号
     */
    private OrderEntity buildOrder(OrderSubmitVO orderSubmitVO, MemberResponseVO memberResponseVO, String orderSn) {
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(orderSn);

        // 2.1、设置用户信息
        orderEntity.setMemberId(memberResponseVO.getId())
                .setMemberUsername(memberResponseVO.getUsername());

        // 2.2、设置邮费和收件人信息
        FareVO fareVO = this.wareFeignService.getFare(orderSubmitVO.getAddrId());
        BigDecimal fare = fareVO.getFare();
        MemberAddressVO address = fareVO.getAddress();

        orderEntity.setFreightAmount(fare)
                .setReceiverName(address.getName())
                .setReceiverPhone(address.getPhone())
                .setReceiverPostCode(address.getPostCode())
                .setReceiverProvince(address.getProvince())
                .setReceiverCity(address.getCity())
                .setReceiverRegion(address.getRegion())
                .setReceiverDetailAddress(address.getDetailAddress());

        // 2.3、设置订单相关的状态信息
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode())
                .setConfirmStatus(0)
                .setAutoConfirmDay(7);

        return orderEntity;
    }


    /**
     * 构建所有订单项
     */
    private List<OrderItemEntity> buildOrderItems(String orderSn) {
        List<OrderItemVO> checkedItems = this.cartFeignService.getCheckedItems();

        return checkedItems.stream()
                .map((item) -> {
                    OrderItemEntity orderItemEntity = buildOrderItem(item);
                    // 设置订单号
                    orderItemEntity.setOrderSn(orderSn);
                    return orderItemEntity;
                })
                .collect(Collectors.toList());
    }


    /**
     * 构建每个订单项
     */
    private OrderItemEntity buildOrderItem(OrderItemVO item) {
        OrderItemEntity orderItemEntity = new OrderItemEntity();

        Long skuId = item.getSkuId();
        // 3.1、设置sku相关属性
        orderItemEntity.setSkuId(skuId)
                .setSkuName(item.getTitle())
                // 集合转字符串
                .setSkuAttrsVals(StringUtils.collectionToDelimitedString(item.getSkuAttrValues(), ";"))
                .setSkuPic(item.getImage())
                .setSkuPrice(item.getPrice())
                .setSkuQuantity(item.getCount());

        // 3.2、通过 skuId 查询 spu 相关属性并设置
        R r = this.productFeignService.getSpuInfoBySkuId(skuId);
        if (r.getCode() == R_SUCCESS_CODE) {
            SpuInfoDTO spuInfoDTO = r.parseObjectFromMap("spuInfo", new TypeReference<SpuInfoDTO>() {
            });
            orderItemEntity.setSpuId(spuInfoDTO.getId())
                    .setSpuName(spuInfoDTO.getSpuName())
                    .setSpuBrand(spuInfoDTO.getBrandName())
                    .setCategoryId(spuInfoDTO.getCatalogId());
        }

        // 3.3、商品的优惠信息(不做)

        // 3.4、商品的积分成长，为价格*数量
        orderItemEntity.setGiftGrowth(item.getPrice().multiply(new BigDecimal(item.getCount())).intValue())
                .setGiftIntegration(item.getPrice().multiply(new BigDecimal(item.getCount())).intValue());

        // 3.5、订单项订单价格信息
        orderItemEntity.setPromotionAmount(BigDecimal.ZERO)
                .setCouponAmount(BigDecimal.ZERO)
                .setIntegrationAmount(BigDecimal.ZERO);

        // 3.6、实际价格
        BigDecimal origin = orderItemEntity.getSkuPrice().multiply(new BigDecimal(orderItemEntity.getSkuQuantity()));
        BigDecimal realPrice = origin.subtract(orderItemEntity.getPromotionAmount())
                .subtract(orderItemEntity.getCouponAmount())
                .subtract(orderItemEntity.getIntegrationAmount());

        orderItemEntity.setRealAmount(realPrice);

        return orderItemEntity;
    }


    /**
     * 比较前端的总价和数据库总价
     */
    private void computePrice(OrderEntity entity, List<OrderItemEntity> orderItemEntities) {
        // 总价
        BigDecimal total = BigDecimal.ZERO;

        // 优惠价格
        BigDecimal promotion = BigDecimal.ZERO;
        BigDecimal integration = BigDecimal.ZERO;
        BigDecimal coupon = BigDecimal.ZERO;

        // 积分
        Integer integrationTotal = 0;
        Integer growthTotal = 0;

        for (OrderItemEntity orderItemEntity : orderItemEntities) {
            total = total.add(orderItemEntity.getRealAmount());
            promotion = promotion.add(orderItemEntity.getPromotionAmount());
            integration = integration.add(orderItemEntity.getIntegrationAmount());
            coupon = coupon.add(orderItemEntity.getCouponAmount());
            integrationTotal += orderItemEntity.getGiftIntegration();
            growthTotal += orderItemEntity.getGiftGrowth();
        }

        entity.setTotalAmount(total)
                .setPromotionAmount(promotion)
                .setIntegrationAmount(integration)
                .setCouponAmount(coupon)
                .setIntegration(integrationTotal)
                .setGrowth(growthTotal)
                // 付款价格=商品价格+运费
                .setPayAmount(entity.getFreightAmount().add(total))
                // 设置删除状态(0-未删除，1-已删除)
                .setDeleteStatus(0);
    }


    private void saveOrder(OrderCreateDTO orderCreateDTO) {
        OrderEntity orderEntity = orderCreateDTO.getOrder();
        orderEntity.setCreateTime(new Date())
                .setModifyTime(new Date());

        this.save(orderEntity);
        this.orderItemService.saveBatch(orderCreateDTO.getOrderItems());
    }
}