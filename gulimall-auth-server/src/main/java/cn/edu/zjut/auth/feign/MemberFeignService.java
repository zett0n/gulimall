package cn.edu.zjut.auth.feign;

import cn.edu.zjut.auth.vo.UserLoginVO;
import cn.edu.zjut.auth.vo.UserRegistVO;
import cn.edu.zjut.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("gulimall-member")
public interface MemberFeignService {

    @PostMapping("member/member/register")
    R register(@RequestBody UserRegistVO vo);

    @PostMapping(value = "member/member/login")
    R login(@RequestBody UserLoginVO vo);
}
