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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import static cn.edu.zjut.common.constant.CartConstant.CART_PREFIX;

@Service
public class CartServiceImpl implements CartService {

    private StringRedisTemplate stringRedisTemplate;

    // 封装 redis hashmap 操作
    private BoundHashOperations<String, Object, Object> cartOps;

    private ProductFeignService productFeignService;

    private ThreadPoolExecutor executor;

    @Autowired
    public CartServiceImpl(StringRedisTemplate stringRedisTemplate, ProductFeignService productFeignService, ThreadPoolExecutor executor) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.productFeignService = productFeignService;
        this.executor = executor;

        LoginInfoDTO loginInfoDTO = CartInterceptor.threadLocal.get();
        String redisKey = "";
        if (loginInfoDTO.getUserId() != null) {
            redisKey = CART_PREFIX + loginInfoDTO.getUserId();
        } else {
            redisKey = CART_PREFIX + loginInfoDTO.getVisitorId();
        }
        // 指定 hashmap 本身在 redis 中的 key
        this.cartOps = this.stringRedisTemplate.boundHashOps(redisKey);
    }

    @Override
    public CartItemVO addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException {
        CartItemVO cartItemVO = new CartItemVO();

        // 异步、远程查询当前要添加商品的详细信息
        CompletableFuture<Void> getSkuInfoTask = CompletableFuture.runAsync(() -> {
            R r = this.productFeignService.info(skuId);

            SkuInfoVO skuInfoVO = r.parseObjectFromMap("skuInfo", new TypeReference<SkuInfoVO>() {
            });

            cartItemVO.setCheck(true)
                    .setCount(num)
                    .setImage(skuInfoVO.getSkuDefaultImg())
                    .setTitle(skuInfoVO.getSkuTitle())
                    .setSkuId(skuId)
                    .setPrice(skuInfoVO.getPrice());
        }, this.executor);

        // 异步、远程查询 sku 组合信息
        CompletableFuture<Void> getSkuSaleAttrValuesTask = CompletableFuture.runAsync(() -> {
            List<String> skuSaleAttrValues = this.productFeignService.getSkuSaleAttrValues(skuId);
            cartItemVO.setSkuAttrValues(skuSaleAttrValues);
        }, this.executor);

        CompletableFuture.allOf(getSkuInfoTask, getSkuSaleAttrValuesTask).get();

        // 存入 redis hashmap
        this.cartOps.put(skuId.toString(), JSON.toJSONString(cartItemVO));

        return cartItemVO;
    }

    @Override
    public CartItemVO getCartItem(Long skuId) {
        return null;
    }

    @Override
    public CartVO getCart() throws ExecutionException, InterruptedException {
        return null;
    }

    @Override
    public void clearCartInfo(String cartKey) {

    }

    @Override
    public void checkItem(Long skuId, Integer check) {

    }

    @Override
    public void changeItemCount(Long skuId, Integer num) {

    }

    @Override
    public void deleteIdCartInfo(Integer skuId) {

    }

    @Override
    public List<CartItemVO> getUserCartItems() {
        return null;
    }
}
