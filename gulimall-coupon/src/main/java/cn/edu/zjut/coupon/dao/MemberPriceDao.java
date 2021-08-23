package cn.edu.zjut.coupon.dao;

import cn.edu.zjut.coupon.entity.MemberPriceEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品会员价格
 * 
 * @author zett0n
 * @email d673326004@163.com
 * @date 2021-08-24 04:38:47
 */
@Mapper
public interface MemberPriceDao extends BaseMapper<MemberPriceEntity> {
	
}
