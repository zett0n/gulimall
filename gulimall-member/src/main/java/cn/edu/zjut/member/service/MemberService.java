package cn.edu.zjut.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import cn.edu.zjut.common.utils.PageUtils;
import cn.edu.zjut.member.entity.MemberEntity;

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
}

