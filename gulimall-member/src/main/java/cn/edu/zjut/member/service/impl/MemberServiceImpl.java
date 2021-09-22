package cn.edu.zjut.member.service.impl;

import cn.edu.zjut.common.exception.BizException;
import cn.edu.zjut.common.exception.EmBizError;
import cn.edu.zjut.common.utils.PageUtils;
import cn.edu.zjut.common.utils.Query;
import cn.edu.zjut.member.dao.MemberDao;
import cn.edu.zjut.member.dao.MemberLevelDao;
import cn.edu.zjut.member.entity.MemberEntity;
import cn.edu.zjut.member.entity.MemberLevelEntity;
import cn.edu.zjut.member.service.MemberService;
import cn.edu.zjut.member.vo.MemberUserLoginVO;
import cn.edu.zjut.member.vo.MemberUserRegisterVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

@Slf4j
@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    private MemberLevelDao memberLevelDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void register(MemberUserRegisterVO vo) throws BizException {
        MemberEntity memberEntity = new MemberEntity();

        // 检查用户名和手机号是否存在，存在则抛出异常
        if (checkExist("username", vo.getUserName())) {
            throw new BizException(EmBizError.USERNAME_EXIST_EXCEPTION);
        }

        if (checkExist("mobile", vo.getPhone())) {
            throw new BizException(EmBizError.PHONE_EXIST_EXCEPTION);
        }

        memberEntity.setNickname(vo.getUserName());
        memberEntity.setUsername(vo.getUserName());
        memberEntity.setMobile(vo.getPhone());

        // 密码进行 MD5 盐值加密
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String encode = bCryptPasswordEncoder.encode(vo.getPassword());
        memberEntity.setPassword(encode);

        // 设置默认等级
        MemberLevelEntity levelEntity = this.memberLevelDao.getDefaultLevel();
        memberEntity.setLevelId(levelEntity.getId());

        // 设置其它的默认信息
        memberEntity.setGender(0);
        memberEntity.setCreateTime(new Date());

        // 保存数据
        this.baseMapper.insert(memberEntity);
    }

    /**
     * 判断字段是否在 ums_member 中存在
     *
     * @param field 数据库表中的字段名
     * @param value 要检验的值
     * @return 存在 true
     */
    @Override
    public boolean checkExist(String field, String value) {
        Integer count = this.baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq(field, value));
        return count != 0;
    }

    @Override
    public MemberEntity login(MemberUserLoginVO vo) {
        String loginacct = vo.getLoginacct();
        String password = vo.getPassword();

        MemberEntity memberEntity = this.baseMapper.selectOne(
                new QueryWrapper<MemberEntity>().eq("username", loginacct).or().eq("mobile", loginacct));

        if (memberEntity == null) {
            return null;
        }

        // 密码验证
        String passwordDB = memberEntity.getPassword();
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        // 参数顺序不能反
        boolean matches = bCryptPasswordEncoder.matches(password, passwordDB);

        if (!matches) {
            return null;
        }
        return memberEntity;
    }
}