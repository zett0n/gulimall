package cn.edu.zjut.service.impl;


import cn.edu.zjut.common.constant.SeckillConstant;
import cn.edu.zjut.common.dto.SeckillSessionWithSkusDTO;
import cn.edu.zjut.common.dto.SeckillSkuRedisDTO;
import cn.edu.zjut.common.dto.SkuInfoDTO;
import cn.edu.zjut.common.dto.mq.SeckillOrderDTO;
import cn.edu.zjut.common.utils.R;
import cn.edu.zjut.common.vo.MemberResponseVO;
import cn.edu.zjut.feign.CouponFeignService;
import cn.edu.zjut.feign.ProductFeignService;
import cn.edu.zjut.interceptor.SeckillLoginInterceptor;
import cn.edu.zjut.service.SeckillService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static cn.edu.zjut.common.constant.DefaultConstant.R_SUCCESS_CODE;

@Service
public class SeckillServiceImpl implements SeckillService {

    private CouponFeignService couponFeignService;

    private StringRedisTemplate stringRedisTemplate;

    private ProductFeignService productFeignService;

    private RedissonClient redissonClient;

    private RabbitTemplate rabbitTemplate;

    private BoundHashOperations<String, String, String> hashOps;

    public SeckillServiceImpl(CouponFeignService couponFeignService, StringRedisTemplate stringRedisTemplate,
                              ProductFeignService productFeignService, RedissonClient redissonClient, RabbitTemplate rabbitTemplate) {
        this.couponFeignService = couponFeignService;
        this.stringRedisTemplate = stringRedisTemplate;
        this.productFeignService = productFeignService;
        this.redissonClient = redissonClient;
        this.rabbitTemplate = rabbitTemplate;
        this.hashOps = this.stringRedisTemplate.boundHashOps(SeckillConstant.SKUS_CACHE_PREFIX);
    }

    /**
     * 上架近日秒杀商品
     */
    @Override
    public void uploadSeckillSkuInDays(Integer days) {
        // TODO 缓存设置 ttl,超时释放库存
        // TODO 远程锁库存
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
        sessions.forEach(session -> {
            session.getRelations().forEach(seckillSkuVO -> {
                String key = seckillSkuVO.getPromotionSessionId() + "-" + seckillSkuVO.getSkuId();

                // 判断当前活动关联商品信息是否保存过
                if (Boolean.FALSE.equals(this.hashOps.hasKey(key))) {
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
                    this.hashOps.put(key, JSON.toJSONString(redisDTO));

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
            if (startTime <= currentTime && currentTime <= endTime) {
                // 获取 list 所有元素
                List<String> items = this.stringRedisTemplate.opsForList().range(key, 0, -1);
                return items.stream()
                        .map(item -> JSON.parseObject(this.hashOps.get(item), SeckillSkuRedisDTO.class))
                        .collect(Collectors.toList());
            }
        }
        return Collections.emptyList();
    }


    @Override
    public SeckillSkuRedisDTO getSeckillSkuInfo(Long skuId) {
        Set<String> keys = this.hashOps.keys();
        long currentTime = System.currentTimeMillis();

        List<SeckillSkuRedisDTO> collect = keys.stream()
                // 按 skuId 筛选 key
                .filter(key -> Pattern.matches("\\d-" + skuId, key))
                // 转为 SeckillSkuRedisDTO 对象
                .map(key -> {
                    String value = this.hashOps.get(key);
                    return JSON.parseObject(value, SeckillSkuRedisDTO.class);
                })
                // 过滤掉已经过期的秒杀信息
                .filter(redisDTO -> redisDTO.getEndTime() > currentTime)
                // 按开始时间从早到晚排序
                .sorted(Comparator.comparingLong(SeckillSkuRedisDTO::getStartTime))
                .collect(Collectors.toList());

        // 符合条件的秒杀信息不存在
        if (collect.isEmpty()) {
            return null;
        }

        // 取出最近的秒杀信息
        SeckillSkuRedisDTO redisDTO = collect.get(0);

        // 如果当前未开始秒杀，隐藏商品随机码
        if (currentTime < redisDTO.getStartTime()) {
            redisDTO.setRandomCode(null);
        }
        return redisDTO;
    }


    @Override
    public String kill(String key, String code, Integer num) throws InterruptedException {
        // TODO jsr303
        /* -------------------------------------------- 校验流程 -------------------------------------------- */

        // 1、校验 key
        String redisValue = this.hashOps.get(key);
        if (StringUtils.isEmpty(redisValue)) {
            return null;
        }
        SeckillSkuRedisDTO redisDTO = JSON.parseObject(redisValue, SeckillSkuRedisDTO.class);

        // 2、验证时效
        long currentTime = System.currentTimeMillis();
        if (currentTime < redisDTO.getStartTime() || currentTime > redisDTO.getEndTime()) {
            return null;
        }

        // 3. 校验库存和购买量是否符合要求
        if (num > redisDTO.getSeckillLimit()) {
            return null;
        }

        // 4、验证商品随机码是否对应
        if (!redisDTO.getRandomCode().equals(code)) {
            return null;
        }

        // 5、通过 redis SETNX userId-sessionId-skuId 验证当前用户是否购买过
        MemberResponseVO memberResponseVO = SeckillLoginInterceptor.loginUser.get();
        String occupyKey = SeckillConstant.KILL_LOCK + memberResponseVO.getId() + "-" + redisDTO.getPromotionSessionId() + "-" + redisDTO.getSkuId();
        long ttl = redisDTO.getEndTime() - System.currentTimeMillis();

        Boolean occupy = this.stringRedisTemplate.opsForValue().setIfAbsent(occupyKey, num.toString(), ttl, TimeUnit.MILLISECONDS);
        // 占位失败，证明已经购买
        if (Boolean.FALSE.equals(occupy)) {
            return null;
        }

        /* -------------------------------------------- 抢购流程 -------------------------------------------- */

        // 获取库存信号量
        RSemaphore semaphore = this.redissonClient.getSemaphore(SeckillConstant.SKU_STOCK_SEMAPHORE + redisDTO.getRandomCode());
        // tryAcquire 没获得到信号量不会阻塞
        boolean acquire = semaphore.tryAcquire(num, 20, TimeUnit.MILLISECONDS);

        // 抢购失败
        if (!acquire) {
            return null;
        }

        /* -------------------------------------------- 下单流程 -------------------------------------------- */

        // 创建订单
        // TODO 订单号分布式下一致性?雪花算法
        String orderSn = IdWorker.getTimeId();
        SeckillOrderDTO seckillOrderDTO = new SeckillOrderDTO();
        seckillOrderDTO.setOrderSn(orderSn)
                .setMemberId(memberResponseVO.getId())
                .setPromotionSessionId(redisDTO.getPromotionSessionId())
                .setSkuId(redisDTO.getSkuId())
                .setNum(num)
                .setSeckillPrice(redisDTO.getSeckillPrice());

        // 发送消息
        this.rabbitTemplate.convertAndSend("order-event-exchange", "order.seckill", seckillOrderDTO);

        return orderSn;
    }

}
