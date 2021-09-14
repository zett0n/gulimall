package cn.edu.zjut.product;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

@SpringBootTest
public class RedisTest {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    public void test() {
        this.stringRedisTemplate.opsForValue().set("hello", "world");
        System.out.println(this.stringRedisTemplate.opsForValue().get("hello2"));
    }
}
