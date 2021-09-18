package cn.edu.zjut.product.web;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("redisson")
@Slf4j
public class RedissonTests {

    private final RedissonClient redisson;
    private final ValueOperations<String, String> ops;

    @Autowired
    public RedissonTests(RedissonClient redisson, StringRedisTemplate stringRedisTemplate) {
        this.redisson = redisson;
        this.ops = stringRedisTemplate.opsForValue();
    }

    /**
     * Redisson 实现分布式锁
     * RLock 类似 ReentrantLock
     * <p>
     * 如果我们指定了锁的超时时间（ttl），就发送给 redis 执行脚本，进行占锁（没有锁的自动续期）
     * 因为锁到期后不会自动续期，锁的超时时间一定要大于业务执行时间，否则又会有不止一个线程进入锁
     * <p>
     * 如果没有指定锁的超时时间，默认采用【看门狗时间】，发送给 redis 执行脚本，进行占锁，并额外附加【锁的自动续期】
     * 【锁的自动续期】如果业务时间很长，运行期间通过【定时任务】自动给锁续期，不用担心业务时间过长，锁自动过期被删掉
     * 加锁的业务只要运行完成，就不会给当前锁续期，即使不手动解锁，默认也会在 30s 后解锁  // TODO 如何判断加锁的业务运行完成？
     * <p>
     * 【看门狗时间（30s)】lockWatchdogTimeout = 30 * 1000;
     * 【定时任务】重新给锁设置过期时间，新的过期时间就是看门狗时间
     * 【定时任务间隔（10s)】internalLockLeaseTime = lockWatchdogTimeout / 3;
     * <p>
     * 最佳实践：
     * 指定锁的超时时间，设置超时时间长点，省去自动续期，直接手动解锁或者超时释放锁
     * 如果业务真的超时到了锁的超时时间，证明业务肯定出了问题，此时放别的线程进来是可以接受的
     */
    @GetMapping
    public String testRedisson() {
        RLock redissonLock = this.redisson.getLock("redissonLock");
        try {
            // 默认 ttl 30s
            // redissonLock.lock();
            redissonLock.lock(20, TimeUnit.SECONDS);
            log.debug("加锁成功，执行业务..." + Thread.currentThread().getId());
            TimeUnit.SECONDS.sleep(15);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            log.debug("释放锁..." + Thread.currentThread().getId());
            redissonLock.unlock();
        }
        return "hello redisson";
    }

    /**
     * 保证一定能读到最新数据，修改期间，写锁是一个排它锁（互斥锁、独享锁），读锁是一个共享锁
     * 写锁没释放读锁必须等待
     * 读 + 读 ：相当于无锁，并发读，只会在Redis中记录好，所有当前的读锁。他们都会同时加锁成功
     * 写 + 读 ：必须等待写锁释放
     * 写 + 写 ：阻塞方式
     * 读 + 写 ：有读锁。写也需要等待
     * 只要有读或者写的存都必须等待
     */
    @GetMapping(value = "/read")
    public String read() {
        String s = "";
        RReadWriteLock readWriteLock = this.redisson.getReadWriteLock("rw-lock");
        RLock readLock = readWriteLock.readLock();

        try {
            // 加读锁
            readLock.lock();
            log.debug("读数据...");
            s = this.ops.get("writeValue");
            try {
                TimeUnit.SECONDS.sleep(4);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            readLock.unlock();
        }
        return s;
    }

    @GetMapping(value = "/write")
    public String write() {
        String s = "";
        RReadWriteLock readWriteLock = this.redisson.getReadWriteLock("rw-lock");
        RLock writeLock = readWriteLock.writeLock();

        try {
            // 加写锁
            writeLock.lock();
            log.debug("写数据...");
            s = UUID.randomUUID().toString();
            this.ops.set("writeValue", s);
            TimeUnit.SECONDS.sleep(8);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            writeLock.unlock();
        }
        return s;
    }

    /**
     * 信号量也可以做分布式限流
     * 车库停车案例
     */
    @GetMapping(value = "/park")
    public String park() throws InterruptedException {

        RSemaphore park = this.redisson.getSemaphore("park");
        // 获取一个信号（阻塞式）
        park.acquire();
        return "park ok";

        // 获取一个信号（非阻塞式）
        // boolean flag = park.tryAcquire();
        // if (!flag) {
        //     return "error";
        // }
        // 执行业务...
        // return "park?" + flag;
    }

    @GetMapping(value = "/unpark")
    public String unpark() {
        RSemaphore park = this.redisson.getSemaphore("park");
        // 释放一个信号
        park.release();
        return "unpark ok.";
    }

    /**
     * 分布式闭锁
     * 放假锁门案例：5个班，全部走完，我们才可以锁大门
     */
    @GetMapping("/lockDoor")
    public String lockDoor() throws InterruptedException {
        RCountDownLatch door = this.redisson.getCountDownLatch("door");
        door.trySetCount(5);
        // 等待闭锁完成
        door.await();
        return "放假了...";
    }

    @GetMapping("/comeHome/{id}")
    public String comeHome(@PathVariable("id") Long id) {
        RCountDownLatch door = this.redisson.getCountDownLatch("door");
        // 计数-1
        door.countDown();
        return id + "班的人都走了...";
    }
}
