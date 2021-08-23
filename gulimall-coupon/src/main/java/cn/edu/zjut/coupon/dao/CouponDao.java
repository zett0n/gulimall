package cn.edu.zjut.coupon.dao;

import cn.edu.zjut.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author zett0n
 * @email d673326004@163.com
 * @date 2021-08-24 04:38:47
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
