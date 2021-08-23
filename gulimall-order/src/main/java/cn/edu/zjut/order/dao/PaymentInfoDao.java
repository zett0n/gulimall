package cn.edu.zjut.order.dao;

import cn.edu.zjut.order.entity.PaymentInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 支付信息表
 * 
 * @author zett0n
 * @email d673326004@163.com
 * @date 2021-08-24 05:14:57
 */
@Mapper
public interface PaymentInfoDao extends BaseMapper<PaymentInfoEntity> {
	
}
