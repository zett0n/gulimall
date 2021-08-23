package cn.edu.zjut.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import cn.edu.zjut.common.utils.PageUtils;
import cn.edu.zjut.ware.entity.PurchaseDetailEntity;

import java.util.Map;

/**
 * 
 *
 * @author zett0n
 * @email d673326004@163.com
 * @date 2021-08-24 05:50:17
 */
public interface PurchaseDetailService extends IService<PurchaseDetailEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

