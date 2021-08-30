package cn.edu.zjut.ware.service.impl;

import cn.edu.zjut.common.utils.PageUtils;
import cn.edu.zjut.common.utils.Query;
import cn.edu.zjut.ware.dao.PurchaseDetailDao;
import cn.edu.zjut.ware.entity.PurchaseDetailEntity;
import cn.edu.zjut.ware.service.PurchaseDetailService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service("purchaseDetailService")
public class PurchaseDetailServiceImpl extends ServiceImpl<PurchaseDetailDao, PurchaseDetailEntity> implements PurchaseDetailService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        /**
         * status: 0,//状态
         * wareId: 1,//仓库id
         */
        QueryWrapper<PurchaseDetailEntity> queryWrapper = new QueryWrapper<>();

        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            //purchase_id  sku_id
            queryWrapper.and(w -> w.eq("purchase_id", key).or().eq("sku_id", key));
        }

        String status = (String) params.get("status");
        if (!StringUtils.isEmpty(status)) {
            //purchase_id  sku_id
            queryWrapper.eq("status", status);
        }

        String wareId = (String) params.get("wareId");
        if (!StringUtils.isEmpty(wareId)) {
            //purchase_id  sku_id
            queryWrapper.eq("ware_id", wareId);
        }

        IPage<PurchaseDetailEntity> page = this.page(new Query<PurchaseDetailEntity>().getPage(params), queryWrapper);
        return new PageUtils(page);
    }

    @Override
    public List<PurchaseDetailEntity> listDetailByPurchaseId(Long id) {
        return this.list(new QueryWrapper<PurchaseDetailEntity>().eq("purchase_id", id));
    }

}