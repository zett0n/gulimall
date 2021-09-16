package cn.edu.zjut.product;

import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

@SpringBootTest
public class RedisTests {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Test
    public void testRedis() {
        this.stringRedisTemplate.opsForValue().set("hello", "world");
        System.out.println(this.stringRedisTemplate.opsForValue().get("hello2"));
    }

    @Test
    public void testRedisson() {
        System.out.println(redissonClient);
    }
    
}
