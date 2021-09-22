package cn.edu.zjut.auth.web;

import cn.edu.zjut.auth.feign.MemberFeignService;
import cn.edu.zjut.auth.feign.ThirdPartyFeignService;
import cn.edu.zjut.auth.vo.UserLoginVO;
import cn.edu.zjut.auth.vo.UserRegistVO;
import cn.edu.zjut.common.exception.EmBizError;
import cn.edu.zjut.common.utils.R;
import cn.edu.zjut.common.vo.MemberResponseVO;
import com.alibaba.fastjson.TypeReference;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static cn.edu.zjut.common.constant.AuthServerConstant.*;
import static cn.edu.zjut.common.constant.DefaultConstant.R_SUCCESS_CODE;

/**
 * WebConfig 的试图映射直接将路径映射完毕
 */
@Controller
public class LoginController {

    private final ThirdPartyFeignService thirdPartyFeignService;

    private final StringRedisTemplate stringRedisTemplate;

    private final ValueOperations<String, String> ops;

    private final MemberFeignService memberFeignService;

    private final String registAddr;
    private final String loginAddr;
    private final String indextAddr;

    @Autowired
    public LoginController(ThirdPartyFeignService thirdPartyFeignService, StringRedisTemplate stringRedisTemplate,
                           MemberFeignService memberFeignService, @Value("${server.servlet.session.cookie.domain}") String domain) {
        this.thirdPartyFeignService = thirdPartyFeignService;
        this.stringRedisTemplate = stringRedisTemplate;
        this.memberFeignService = memberFeignService;
        this.ops = stringRedisTemplate.opsForValue();
        this.registAddr = MessageFormat.format("redirect:http://auth.{0}/reg.html", domain);
        this.loginAddr = MessageFormat.format("redirect:http://auth.{0}/login.html", domain);
        this.indextAddr = MessageFormat.format("redirect:http://{0}", domain);
    }

    @GetMapping("/login.html")
    public String loginPage(HttpSession session) {
        // 已登录，跳转至首页
        if (session.getAttribute(LOGIN_USER) != null) {
            return this.indextAddr;
        }
        return "login";
    }

    /*
     * 随机生成验证码存入 redis，然后远程调取第三方 sms 服务将手机验证码短信发送用户
     */
    @GetMapping("/sms/sendCode")
    @ResponseBody
    public R sendCode(@RequestParam("phone") String phone) {
        // 前缀 + 手机号存入 redis
        String redisKey = SMS_CODE_CACHE_PREFIX + phone;

        // 4位数字验证码
        String code;

        // 判断验证码是否过期（300s内不能再发）
        String redisValue = this.ops.get(redisKey);
        if (StringUtils.isNotEmpty(redisValue)) {
            return R.error(EmBizError.SMS_CODE_EXCEPTION);
        }
        // redis 中未获取到信息，生成验证码
        code = String.valueOf((int) (1000 * (Math.random() * 9 + 1)));

        redisValue = code;
        this.ops.set(redisKey, redisValue, SMS_CODE_TTL, TimeUnit.SECONDS);

        // 远程调取第三方 sms 服务
        this.thirdPartyFeignService.sendCode(phone, code);

        return R.ok();
    }


    /**
     * 注册主题逻辑：
     * 1、若 JSR303 校验未通过，则通过 BindingResult 封装错误信息，并重定向至注册页面
     * 2、若通过 JSR303 校验，则需要从 redis 中取值判断验证码是否正确，正确的话通过会员服务注册
     * 3、会员服务调用成功则重定向至登录页，否则封装远程服务返回的错误信息返回至注册页面
     *
     * @param redirectAttributes 可以通过 session 保存信息并在重定向的时候携带过去
     */
    @PostMapping("/register")
    public String register(@Valid UserRegistVO userRegistVO, BindingResult result, RedirectAttributes redirectAttributes) {
        Map<String, String> errors = new HashMap<>();

        // 如果校验参数失败，回到注册页面
        if (result.hasErrors()) {
            // 封装错误信息（Collectors.toMap 会报错，使用 forEach 代替）
            // errors = result.getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            result.getFieldErrors().forEach(fieldError -> {
                errors.put(fieldError.getField(), fieldError.getDefaultMessage());
            });

            // 只要跳转到下个页面取出这个数据后，session 中的数据就会删掉
            redirectAttributes.addFlashAttribute("errors", errors);

            /*
             * 使用什么方式回到注册页面？
             * 1、转发：
             *  return "forward:/reg.html";
             *      会发生 Request method 'POST' not supported 异常
             *      因为 register 方法为 Post 请求，而 WebConfig 中配置的路径映射使用 GET 请求
             *
             *  return "reg";
             *      重新转发给 thymeleaf 渲染，但如果用户再点击刷新，浏览器会提示重新提交表单
             *
             * 2、重定向：
             *  return "redirect:/reg.html";
             *      注意，重定向会使用服务器 ip+port 地址（http://192.168.0.104:20000/reg.html），这里我们需要带域名的方式
             */
            return this.registAddr;
        }

        // 判断验证码
        String code = userRegistVO.getCode();
        String redisKey = SMS_CODE_CACHE_PREFIX + userRegistVO.getPhone();
        String redisValue = this.ops.get(redisKey);

        // redis 中未找到验证码或者验证码错误
        if (StringUtils.isEmpty(redisValue) || !redisValue.equalsIgnoreCase(code)) {
            errors.put("code", "验证码错误");
            redirectAttributes.addFlashAttribute("errors", errors);
            return this.registAddr;
        }

        // 验证码通过，将失效的验证码删除
        this.stringRedisTemplate.delete(redisKey);

        // 调用远程服务注册
        R r = this.memberFeignService.register(userRegistVO);

        // 远程出现异常，查看 map 中封装的错误消息
        if (r.getCode() != R_SUCCESS_CODE) {
            errors.put("msg", r.getMsg());
            redirectAttributes.addFlashAttribute("errors", errors);
            return this.registAddr;
        }

        // 注册成功，跳转到登录页
        return this.loginAddr;
    }

    @PostMapping(value = "/login")
    public String login(UserLoginVO vo, RedirectAttributes attributes, HttpSession session) {

        // 远程登录
        R r = this.memberFeignService.login(vo);

        // 登录失败
        if (r.getCode() != R_SUCCESS_CODE) {
            Map<String, String> errors = new HashMap<>();
            errors.put("msg", r.getMsg());
            attributes.addFlashAttribute("errors", errors);

            return this.loginAddr;
        }

        // 登录成功，将用户信息放入 session
        MemberResponseVO data = r.parseObjectFromMap("data", new TypeReference<MemberResponseVO>() {
        });
        session.setAttribute(LOGIN_USER, data);

        return this.indextAddr;
    }

}

