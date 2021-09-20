package cn.edu.zjut.auth.web;

import cn.edu.zjut.auth.feign.ThirdPartyFeignService;
import cn.edu.zjut.common.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * WebConfig 的试图映射直接将路径映射完毕
 */
@Controller
public class LoginController {

    @Autowired
    private ThirdPartyFeignService thirdPartyFeignService;

    // @GetMapping("/login.html")
    // public String loginPage() {
    //     return "login";
    // }

    // @GetMapping("/reg.html")
    // public String regPage() {
    //     return "reg";
    // }

    @GetMapping(value = "/sms/sendCode")
    @ResponseBody
    public R sendCode(@RequestParam("phone") String phone) {
        String code = "55555";
        this.thirdPartyFeignService.sendCode(phone, code);

        return R.ok();
    }
}

// //1、接口防刷
// String redisCode = stringRedisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);
// if (!StringUtils.isEmpty(redisCode)) {
//     //活动存入redis的时间，用当前时间减去存入redis的时间，判断用户手机号是否在60s内发送验证码
//     long currentTime = Long.parseLong(redisCode.split("_")[1]);
//     if (System.currentTimeMillis() - currentTime < 60000) {
//         //60s内不能再发
//         return R.error(BizCodeEnum.SMS_CODE_EXCEPTION.getCode(), BizCodeEnum.SMS_CODE_EXCEPTION.getMsg());
//     }
// }
//
// //2、验证码的再次效验 redis.存key-phone,value-code
// int code = (int) ((Math.random() * 9 + 1) * 100000);
// String codeNum = String.valueOf(code);
// String redisStorage = codeNum + "_" + System.currentTimeMillis();
//
// //存入redis，防止同一个手机号在60秒内再次发送验证码
// stringRedisTemplate.opsForValue().set(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone,
//         redisStorage, 10, TimeUnit.MINUTES);