package cn.edu.zjut.order.dao;

import cn.edu.zjut.order.entity.OrderSettingEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单配置信息
 * 
 * @author zett0n
 * @email d673326004@163.com
 * @date 2021-08-24 05:14:57
 */
@Mapper
public interface OrderSettingDao extends BaseMapper<OrderSettingEntity> {
	
}
