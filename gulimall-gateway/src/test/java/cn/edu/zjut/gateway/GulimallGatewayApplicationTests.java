package cn.edu.zjut.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GulimallGatewayApplicationTests {
    @Value("${myname}")
    private String myname;

    @Test
    void contextLoads() {
        System.out.println(this.myname);
    }

}
