package cn.edu.zjut.cart.service.impl;

import cn.edu.zjut.cart.dto.LoginInfoDTO;
import cn.edu.zjut.cart.feign.ProductFeignService;
import cn.edu.zjut.cart.interceptor.CartInterceptor;
import cn.edu.zjut.cart.service.CartService;
import cn.edu.zjut.cart.vo.CartItemVO;
import cn.edu.zjut.cart.vo.CartVO;
import cn.edu.zjut.cart.vo.SkuInfoVO;
import cn.edu.zjut.common.utils.R;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import static cn.edu.zjut.common.constant.CartConstant.CART_PREFIX;

@Service
@Slf4j
public class CartServiceImpl implements CartService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private ThreadPoolExecutor executor;


    /**
     * 判断游客是否登录，登录返回 true
     */
    private boolean checkUserLogin() {
        LoginInfoDTO loginInfoDTO = CartInterceptor.threadLocal.get();
        // log.debug("{}", loginInfoDTO);
        return loginInfoDTO.getUserId() != null;
    }


    /**
     * 根据登录信息自动指定 redis 购物车前缀（游客/用户）
     */
    private BoundHashOperations<String, Object, Object> getCartOps() {
        LoginInfoDTO loginInfoDTO = CartInterceptor.threadLocal.get();
        if (checkUserLogin()) {
            return this.stringRedisTemplate.boundHashOps(CART_PREFIX + loginInfoDTO.getUserId());
        } else {
            return this.stringRedisTemplate.boundHashOps(CART_PREFIX + loginInfoDTO.getVisitorId());
        }
    }


    @Override
    public CartItemVO addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException {
        CartItemVO cartItemVO = new CartItemVO();
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();

        // 判断购物车中要添加的商品是否已经存在
        Object hashMapValue = cartOps.get(skuId.toString());
        if (null != hashMapValue) {
            // A.已存在，加数量
            cartItemVO = JSON.parseObject((String) hashMapValue, CartItemVO.class);
            cartItemVO.setCount(cartItemVO.getCount() + num);
        } else {
            // B.不存在，生成商品项
            // 异步、远程查询当前要添加商品的详细信息
            CartItemVO finalCartItemVO = cartItemVO;
            CompletableFuture<Void> getSkuInfoTask = CompletableFuture.runAsync(() -> {
                R r = this.productFeignService.info(skuId);

                SkuInfoVO skuInfoVO = r.parseObjectFromMap("skuInfo", new TypeReference<SkuInfoVO>() {
                });

                finalCartItemVO.setCheck(true)
                        .setCount(num)
                        .setImage(skuInfoVO.getSkuDefaultImg())
                        .setTitle(skuInfoVO.getSkuTitle())
                        .setSkuId(skuId)
                        .setPrice(skuInfoVO.getPrice());
            }, this.executor);

            // 异步、远程查询 sku 组合信息
            CartItemVO finalCartItemVO1 = cartItemVO;
            CompletableFuture<Void> getSkuSaleAttrValuesTask = CompletableFuture.runAsync(() -> {
                List<String> skuSaleAttrValues = this.productFeignService.getSkuSaleAttrValues(skuId);
                finalCartItemVO1.setSkuAttrValues(skuSaleAttrValues);
            }, this.executor);

            CompletableFuture.allOf(getSkuInfoTask, getSkuSaleAttrValuesTask).get();
        }
        // 存入 redis hashmap
        cartOps.put(skuId.toString(), JSON.toJSONString(cartItemVO));

        return cartItemVO;
    }


    @Override
    public CartItemVO getCartItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        Object hashMapValue = cartOps.get(skuId.toString());
        if (null == hashMapValue) {
            return null;
        }
        return JSON.parseObject((String) hashMapValue, CartItemVO.class);
    }


    @Override
    public CartVO getCart() throws ExecutionException, InterruptedException {
        CartVO vo = new CartVO();
        LoginInfoDTO loginInfoDTO = CartInterceptor.threadLocal.get();
        String visitorCartKey = CART_PREFIX + loginInfoDTO.getVisitorId();
        String userCartKey = CART_PREFIX + loginInfoDTO.getUserId();

        // 游客购物车信息总是要获得
        List<CartItemVO> visitorCartItems = getCartItems(visitorCartKey);
        vo.setItems(visitorCartItems);

        // 如果用户登录，合并用户和游客购物车
        if (checkUserLogin()) {
            for (CartItemVO visitorCartItem : visitorCartItems) {
                addToCart(visitorCartItem.getSkuId(), visitorCartItem.getCount());
            }
            List<CartItemVO> userCartItems = getCartItems(userCartKey);
            vo.setItems(userCartItems);

            // 清空游客购物车
            clearCart(visitorCartKey);
        }
        return vo;
    }


    /**
     * 返回游客/用户的所有购物项
     */
    private List<CartItemVO> getCartItems(String cartKey) {
        BoundHashOperations<String, Object, Object> ops = this.stringRedisTemplate.boundHashOps(cartKey);
        List<Object> values = ops.values();
        return values.stream().map(item -> JSON.parseObject((String) item, CartItemVO.class))
                .collect(Collectors.toList());
    }


    @Override
    public void clearCart(String cartKey) {
        this.stringRedisTemplate.delete(cartKey);
    }


    @Override
    public void checkItem(Long skuId, Integer check) {
        // 查询购物车里面的商品
        CartItemVO cartItemVO = getCartItem(skuId);

        // 修改商品状态
        cartItemVO.setCheck(check == 1);

        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.put(skuId.toString(), JSON.toJSONString(cartItemVO));
    }

    @Override
    public void changeItemCount(Long skuId, Integer num) {
        // 查询购物车里面的商品
        CartItemVO cartItemVO = getCartItem(skuId);
        cartItemVO.setCount(num);

        // 存入redis中
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.put(skuId.toString(), JSON.toJSONString(cartItemVO));
    }

    @Override
    public void deleteIdCartInfo(Integer skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.delete(skuId.toString());
    }


    @Override
    public List<CartItemVO> getUserCheckedItems() {
        LoginInfoDTO loginInfoDTO = CartInterceptor.threadLocal.get();
        String userCartKey = CART_PREFIX + loginInfoDTO.getUserId();
        List<CartItemVO> userCartItems = getCartItems(userCartKey);

        return userCartItems.stream()
                .filter(CartItemVO::getCheck)
                .peek(item -> {
                    // 远程查询最新价格 TODO 循环feign
                    BigDecimal price = this.productFeignService.getPrice(item.getSkuId());
                    item.setPrice(price);
                })
                .collect(Collectors.toList());
    }

}
