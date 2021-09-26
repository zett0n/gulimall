package cn.edu.zjut.ware.service.impl;

import cn.edu.zjut.common.utils.PageUtils;
import cn.edu.zjut.common.utils.Query;
import cn.edu.zjut.common.utils.R;
import cn.edu.zjut.common.vo.FareVO;
import cn.edu.zjut.common.vo.MemberAddressVO;
import cn.edu.zjut.ware.dao.WareInfoDao;
import cn.edu.zjut.ware.entity.WareInfoEntity;
import cn.edu.zjut.ware.feign.MemberFeignService;
import cn.edu.zjut.ware.service.WareInfoService;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

import static cn.edu.zjut.common.constant.DefaultConstant.R_SUCCESS_CODE;

@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {

    @Autowired
    private MemberFeignService memberFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareInfoEntity> wareInfoEntityQueryWrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (StringUtils.isNotEmpty(key)) {
            wareInfoEntityQueryWrapper.eq("id", key).or().like("name", key).or().like("address", key).or().like("areacode", key);
        }
        IPage<WareInfoEntity> page = this.page(new Query<WareInfoEntity>().getPage(params), wareInfoEntityQueryWrapper);
        return new PageUtils(page);
    }

    @Override
    public FareVO getFare(Long addrId) {
        FareVO fareVO = new FareVO();
        R r = this.memberFeignService.info(addrId);
        if (r.getCode() == R_SUCCESS_CODE) {
            MemberAddressVO address = r.parseObjectFromMap("memberReceiveAddress", new TypeReference<MemberAddressVO>() {
            });
            fareVO.setAddress(address);

            // TODO 邮费计算
            // 取电话号的最后两位作为邮费
            String phone = address.getPhone();
            String fare = phone.substring(phone.length() - 2);
            fareVO.setFare(new BigDecimal(fare));
        }
        return fareVO;
    }

}