package cn.edu.zjut.ware.service;

import cn.edu.zjut.common.utils.PageUtils;
import cn.edu.zjut.ware.entity.PurchaseEntity;
import cn.edu.zjut.ware.vo.MergeVO;
import cn.edu.zjut.ware.vo.PurchaseDoneVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * 采购信息
 *
 * @author zett0n
 * @email d673326004@163.com
 * @date 2021-08-24 05:50:17
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPageUnreceive(Map<String, Object> params);

    void mergePurchase(MergeVO mergeVO);

    void received(List<Long> purchaseIds);

    void done(PurchaseDoneVO purchaseDoneVO);
}

