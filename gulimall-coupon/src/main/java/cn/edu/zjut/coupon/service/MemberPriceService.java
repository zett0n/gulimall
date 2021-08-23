package cn.edu.zjut.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import cn.edu.zjut.common.utils.PageUtils;
import cn.edu.zjut.coupon.entity.MemberPriceEntity;

import java.util.Map;

/**
 * 商品会员价格
 *
 * @author zett0n
 * @email d673326004@163.com
 * @date 2021-08-24 04:38:47
 */
public interface MemberPriceService extends IService<MemberPriceEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

