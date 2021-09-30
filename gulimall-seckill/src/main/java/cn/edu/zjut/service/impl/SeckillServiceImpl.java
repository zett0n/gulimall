package cn.edu.zjut.service.impl;


import cn.edu.zjut.common.constant.SeckillConstant;
import cn.edu.zjut.common.dto.SeckillSessionWithSkusDTO;
import cn.edu.zjut.common.dto.SeckillSkuRedisDTO;
import cn.edu.zjut.common.dto.SkuInfoDTO;
import cn.edu.zjut.common.utils.R;
import cn.edu.zjut.feign.CouponFeignService;
import cn.edu.zjut.feign.ProductFeignService;
import cn.edu.zjut.service.SeckillService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static cn.edu.zjut.common.constant.DefaultConstant.R_SUCCESS_CODE;

@Service
public class SeckillServiceImpl implements SeckillService {

    @Autowired
    private CouponFeignService couponFeignService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private RedissonClient redissonClient;

    // @Autowired
    // private RabbitTemplate rabbitTemplate;


    /**
     * 上架近日秒杀商品
     */
    @Override
    public void uploadSeckillSkuInDays(Integer days) {
        // 远程查询最近几天需要参与秒杀的商品和秒杀活动
        R r = this.couponFeignService.listInDays(days);
        if (r.getCode() == R_SUCCESS_CODE) {
            List<SeckillSessionWithSkusDTO> sessions = r.parseObjectFromMap("data", new TypeReference<List<SeckillSessionWithSkusDTO>>() {
            });
            // 缓存活动信息
            saveSecKillSession(sessions);
            // 缓存活动关联商品信息
            saveSecKillSku(sessions);
        }
    }


    /**
     * 缓存活动信息
     * K: SESSION_CACHE_PREFIX + startTime + "_" + endTime
     * V: List, sessionId + "-" + skuId
     */
    private void saveSecKillSession(List<SeckillSessionWithSkusDTO> sessions) {
        sessions.forEach(session -> {
            String key = SeckillConstant.SESSIONS_CACHE_PREFIX + session.getStartTime().getTime() + "_" + session.getEndTime().getTime();

            // 判断当前活动信息是否保存过
            if (Boolean.FALSE.equals(this.stringRedisTemplate.hasKey(key))) {
                List<String> values = session.getRelations().stream()
                        .map(seckillSkuVO -> seckillSkuVO.getPromotionSessionId() + "-" + seckillSkuVO.getSkuId())
                        .collect(Collectors.toList());
                this.stringRedisTemplate.opsForList().leftPushAll(key, values);
            }
        });
    }


    /**
     * 缓存活动关联商品信息
     * K: ECKILL_CHARE_PREFIX
     * V: hash，k 为 sessionId + "-" + skuId，v 为对应的商品信息 SeckillSkuRedisDTO
     * <p>
     * 缓存库存信号量
     * K: SKU_STOCK_SEMAPHORE + 商品随机码
     * V: 秒杀的库存件数 + 商品随机码
     */
    private void saveSecKillSku(List<SeckillSessionWithSkusDTO> sessions) {
        BoundHashOperations<String, Object, Object> hashOps = this.stringRedisTemplate.boundHashOps(SeckillConstant.SKUS_CACHE_PREFIX);

        sessions.forEach(session -> {
            session.getRelations().forEach(seckillSkuVO -> {
                String key = seckillSkuVO.getPromotionSessionId() + "-" + seckillSkuVO.getSkuId();

                // 判断当前活动关联商品信息是否保存过
                if (Boolean.FALSE.equals(hashOps.hasKey(key))) {
                    SeckillSkuRedisDTO redisDTO = new SeckillSkuRedisDTO();

                    // 1. 保存 SeckillSkuVo 信息
                    BeanUtils.copyProperties(seckillSkuVO, redisDTO);

                    // 2. 保存开始结束时间
                    redisDTO.setStartTime(session.getStartTime().getTime());
                    redisDTO.setEndTime(session.getEndTime().getTime());

                    // 3. 远程查询 sku 信息并保存
                    R r = this.productFeignService.getSkuInfo(seckillSkuVO.getSkuId());
                    if (r.getCode() == R_SUCCESS_CODE) {
                        SkuInfoDTO skuInfoDTO = r.parseObjectFromMap("skuInfo", new TypeReference<SkuInfoDTO>() {
                        });
                        redisDTO.setSkuInfoDTO(skuInfoDTO);
                    }

                    // 4. 生成商品随机码，防止恶意攻击
                    String token = UUID.randomUUID().toString().replace("-", "");
                    redisDTO.setRandomCode(token);

                    // 5. 序列化为 json 并保存
                    hashOps.put(key, JSON.toJSONString(redisDTO));

                    // 6. 使用库存作为 Redisson 信号量限制库存，限流
                    RSemaphore semaphore = this.redissonClient.getSemaphore(SeckillConstant.SKU_STOCK_SEMAPHORE + token);
                    semaphore.trySetPermits(seckillSkuVO.getSeckillCount());
                }
            });
        });
    }


    /**
     * 获取当前时间的秒杀活动信息，结果返回 SeckillSkuRedisDTO
     * K: SESSION_CACHE_PREFIX + startTime + "_" + endTime
     * V: List, sessionId + "-" + skuId
     */
    @Override
    public List<SeckillSkuRedisDTO> getCurrentSeckillSkus() {
        // 获取所有秒杀场次的 key
        Set<String> keys = this.stringRedisTemplate.keys(SeckillConstant.SESSIONS_CACHE_PREFIX + "*");

        for (String key : keys) {
            // 字符串处理 seckill:sessions:1633073020000_1633363199000，获得两个时间戳
            String replace = key.replace(SeckillConstant.SESSIONS_CACHE_PREFIX, "");
            String[] split = replace.split("_");
            long startTime = Long.parseLong(split[0]);
            long endTime = Long.parseLong(split[1]);

            // 判断现在是否有当前秒杀活动
            long currentTime = System.currentTimeMillis();
            if (currentTime >= startTime && currentTime <= endTime) {
                List<String> items = this.stringRedisTemplate.opsForList().range(key, 0, -1);
                BoundHashOperations<String, Object, Object> hashOps = this.stringRedisTemplate.boundHashOps(SeckillConstant.SKUS_CACHE_PREFIX);

                return items.stream().map(item -> JSON.parseObject((String) hashOps.get(item), SeckillSkuRedisDTO.class))
                        .collect(Collectors.toList());
            }
        }
        return Collections.emptyList();
    }


    // @Override
    // public SeckillSkuRedisDTO getSeckillSkuInfo(Long skuId) {
    //     BoundHashOperations<String, String, String> hashOps = stringRedisTemplate.boundHashOps(SeckillConstant.SKUS_CACHE_PREFIX);
    //     Set<String> keys = hashOps.keys();
    //     for (String key : keys) {
    //         if (Pattern.matches("\\d-" + skuId,key)) {
    //             String v = hashOps.get(key);
    //             SeckillSkuRedisDTO redisDTO = JSON.parseObject(v, SeckillSkuRedisDTO.class);
    //             //当前商品参与秒杀活动
    //             if (redisDTO!=null){
    //                 long current = System.currentTimeMillis();
    //                 //当前活动在有效期，暴露商品随机码返回
    //                 if (redisDTO.getStartTime() < current && redisDTO.getEndTime() > current) {
    //                     return redisDTO;
    //                 }
    //                 redisDTO.setRandomCode(null);
    //                 return redisDTO;
    //             }
    //         }
    //     }
    //     return null;
    // }
    //
    // @Override
    // public String kill(String killId, String key, Integer num) throws InterruptedException {
    //     BoundHashOperations<String, String, String> hashOps = stringRedisTemplate.boundHashOps(SeckillConstant.SKUS_CACHE_PREFIX);
    //     String json = hashOps.get(killId);
    //     String orderSn = null;
    //     if (!StringUtils.isEmpty(json)){
    //         SeckillSkuRedisDTO redisDTO = JSON.parseObject(json, SeckillSkuRedisDTO.class);
    //         //1. 验证时效
    //         long current = System.currentTimeMillis();
    //         if (current >= redisDTO.getStartTime() && current <= redisDTO.getEndTime()) {
    //             //2. 验证商品和商品随机码是否对应
    //             String redisKey = redisDTO.getPromotionSessionId() + "-" + redisDTO.getSkuId();
    //             if (redisKey.equals(killId) && redisDTO.getRandomCode().equals(key)) {
    //                 //3. 验证当前用户是否购买过
    //                 MemberResponseVo memberResponseVo = LoginInterceptor.loginUser.get();
    //                 long ttl = redisDTO.getEndTime() - System.currentTimeMillis();
    //                 //3.1 通过在redis中使用 用户id-skuId 来占位看是否买过
    //                 Boolean occupy = stringRedisTemplate.opsForValue().setIfAbsent(memberResponseVo.getId()+"-"+redisDTO.getSkuId(), num.toString(), ttl, TimeUnit.MILLISECONDS);
    //                 //3.2 占位成功，说明该用户未秒杀过该商品，则继续
    //                 if (occupy){
    //                     //4. 校验库存和购买量是否符合要求
    //                     if (num <= redisDTO.getSeckillLimit()) {
    //                         //4.1 尝试获取库存信号量
    //                         RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + redisDTO.getRandomCode());
    //                         boolean acquire = semaphore.tryAcquire(num,100, TimeUnit.MILLISECONDS);
    //                         //4.2 获取库存成功
    //                         if (acquire) {
    //                             //5. 发送消息创建订单
    //                             //5.1 创建订单号
    //                             orderSn = IdWorker.getTimeId();
    //                             //5.2 创建秒杀订单to
    //                             SeckillOrderTo orderTo = new SeckillOrderTo();
    //                             orderTo.setMemberId(memberResponseVo.getId());
    //                             orderTo.setNum(num);
    //                             orderTo.setOrderSn(orderSn);
    //                             orderTo.setPromotionSessionId(redisDTO.getPromotionSessionId());
    //                             orderTo.setSeckillPrice(redisDTO.getSeckillPrice());
    //                             orderTo.setSkuId(redisDTO.getSkuId());
    //                             //5.3 发送创建订单的消息
    //                             rabbitTemplate.convertAndSend("order-event-exchange", "order.seckill.order", orderTo);
    //                         }
    //                     }
    //                 }
    //             }
    //         }
    //         return orderSn;
    //     }
    //     return null;
    // }

}
