package cn.edu.zjut.member.service;

import cn.edu.zjut.common.utils.PageUtils;
import cn.edu.zjut.member.entity.MemberEntity;
import cn.edu.zjut.member.vo.MemberUserLoginVO;
import cn.edu.zjut.member.vo.MemberUserRegisterVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * 会员
 *
 * @author zett0n
 * @email d673326004@163.com
 * @date 2021-08-24 05:04:00
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void register(MemberUserRegisterVO vo);

    boolean checkExist(String field, String value);

    MemberEntity login(MemberUserLoginVO vo);
}

