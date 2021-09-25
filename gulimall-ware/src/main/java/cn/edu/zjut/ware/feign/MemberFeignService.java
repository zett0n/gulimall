package cn.edu.zjut.ware.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import cn.edu.zjut.common.utils.R;

@FeignClient("gulimall-member")
public interface MemberFeignService {

    @RequestMapping("member/memberreceiveaddress/info/{id}")
    R info(@PathVariable("id") Long id);

}
