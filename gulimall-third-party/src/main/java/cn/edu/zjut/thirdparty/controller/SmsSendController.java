package cn.edu.zjut.thirdparty.controller;

import cn.edu.zjut.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("sms")
public class SmsSendController {

    /**
     * 手机验证码短信发送
     */
    @GetMapping("/sendCode")
    public R sendCode(@RequestParam("phone") String phone, @RequestParam("code") String code) {
        log.debug("phone: {}, code: {}", phone, code);

        // 调用第三方 sms 服务发送验证码
        // smsComponent.sendCode(phone, code);

        return R.ok();
    }
}
