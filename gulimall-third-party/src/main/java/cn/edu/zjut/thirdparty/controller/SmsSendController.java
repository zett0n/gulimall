package cn.edu.zjut.thirdparty.controller;

import cn.edu.zjut.common.utils.R;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/sms")
public class SmsSendController {

    /*
     * 假短信发送
     */
    @GetMapping("/sendCode")
    public R sendCode(@RequestParam("phone") String phone, @RequestParam("code") String code) {
        //发送验证码
        // smsComponent.sendCode(phone, code);
        return R.ok();
    }
}
