package cn.edu.zjut.member;

import cn.edu.zjut.member.service.MemberService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootTest
class GulimallMemberApplicationTests {

    @Autowired
    private MemberService memberService;

    @Test
    public void test() {
        System.out.println(this.memberService.checkExist("username", "asd"));
        System.out.println(this.memberService.checkExist("mobile", "123"));

    }

    @Test
    public void test2() {
        // Spring 盐值加密
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

        // $2a$10$ERR0I4zayIrX2poqCr1oo.I9blSS7GxkRclacGNlh71xWlBU0KPKG
        // $2a$10$wEEVluS0pbCSizFmcSKxf.mi2PGFNuRN/DayLUk2yQAW3d9yNuBWW
        String encode = bCryptPasswordEncoder.encode("123456");
        boolean matches = bCryptPasswordEncoder.matches("123456", "$2a$10$GH2cuowbEeoZl4hGQlT5SOUGk5jJfYuj4aNjb9yNTl4RE6sLFNTrK");

        System.out.println(encode + "==>" + matches);
    }

}
