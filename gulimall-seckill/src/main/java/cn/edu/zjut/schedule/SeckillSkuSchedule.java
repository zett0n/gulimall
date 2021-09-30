package cn.edu.zjut.schedule;

import cn.edu.zjut.common.constant.SeckillConstant;
import cn.edu.zjut.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class SeckillSkuSchedule {

    @Autowired
    private SeckillService seckillService;

    @Autowired
    private RedissonClient redissonClient;

    // 秒分时 日月周
    @Scheduled(cron = "0 * * * * ?")
    public void uploadSeckillSkuInDays() {
        // 上架商品距离天数
        Integer days = 3;

        // 分布式锁，防止并发时多次上架
        RLock lock = this.redissonClient.getLock(SeckillConstant.UPLOAD_LOCK);
        lock.lock(10, TimeUnit.SECONDS);

        try {
            log.info("上架{}天内将要秒杀的商品信息...", days);
            // 接口设计为幂等的，不用担心重复上架（对于单台机器而言）
            this.seckillService.uploadSeckillSkuInDays(days);
        } finally {
            lock.unlock();
        }
    }

}
