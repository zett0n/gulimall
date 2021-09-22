package cn.edu.zjut.member.api;

import cn.edu.zjut.common.exception.BizException;
import cn.edu.zjut.common.exception.EmBizError;
import cn.edu.zjut.common.utils.PageUtils;
import cn.edu.zjut.common.utils.R;
import cn.edu.zjut.member.entity.MemberEntity;
import cn.edu.zjut.member.feign.CouponFeignService;
import cn.edu.zjut.member.service.MemberService;
import cn.edu.zjut.member.vo.MemberUserLoginVO;
import cn.edu.zjut.member.vo.MemberUserRegisterVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;

/**
 * 会员
 *
 * @author zett0n
 * @email d673326004@163.com
 * @date 2021-08-24 05:04:00
 */
@RestController
@RequestMapping("member/member")
public class MemberController {

    @Autowired
    private MemberService memberService;

    @Autowired
    private CouponFeignService couponFeignService;

    // @RequestMapping("/coupons")
    // public R test() {
    //     MemberEntity memberEntity = new MemberEntity();
    //     memberEntity.setNickname("choushao");
    //     R memberList = this.couponFeignService.memberList();
    //     return R.ok().put("member", memberEntity).put("coupons", memberList.get("coupons"));
    // }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public R register(@RequestBody MemberUserRegisterVO vo) throws BizException {
        this.memberService.register(vo);

        return R.ok();
    }

    /**
     * 账号密码登录
     */
    @PostMapping(value = "/login")
    public R login(@RequestBody MemberUserLoginVO vo) {

        MemberEntity memberEntity = this.memberService.login(vo);

        if (memberEntity == null) {
            return R.error(EmBizError.LOGINACCT_PASSWORD_INVALID_EXCEPTION);
        }
        return R.ok().put("data", memberEntity);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = this.memberService.queryPage(params);

        return R.ok().put("page", page);
    }

    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        MemberEntity member = this.memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody MemberEntity member) {
        this.memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody MemberEntity member) {
        this.memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        this.memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
