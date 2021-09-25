package cn.edu.zjut.order.feign;

import cn.edu.zjut.common.vo.MemberAddressVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient("gulimall-member")
public interface MemberFeignService {

    @GetMapping("member/memberreceiveaddress/{memberId}/addresses")
    List<MemberAddressVO> getAddressById(@PathVariable("memberId") Long memberId);
}
