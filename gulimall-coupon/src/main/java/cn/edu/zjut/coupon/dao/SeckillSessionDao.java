package cn.edu.zjut.coupon.dao;

import cn.edu.zjut.coupon.entity.SeckillSessionEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 秒杀活动场次
 * 
 * @author zett0n
 * @email d673326004@163.com
 * @date 2021-08-24 04:38:47
 */
@Mapper
public interface SeckillSessionDao extends BaseMapper<SeckillSessionEntity> {
	
}
