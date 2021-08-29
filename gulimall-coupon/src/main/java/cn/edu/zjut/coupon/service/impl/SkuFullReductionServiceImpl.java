package cn.edu.zjut.coupon.service.impl;

import cn.edu.zjut.common.to.MemberPrice;
import cn.edu.zjut.common.to.SkuReductionTO;
import cn.edu.zjut.common.utils.PageUtils;
import cn.edu.zjut.common.utils.Query;
import cn.edu.zjut.coupon.dao.SkuFullReductionDao;
import cn.edu.zjut.coupon.entity.MemberPriceEntity;
import cn.edu.zjut.coupon.entity.SkuFullReductionEntity;
import cn.edu.zjut.coupon.entity.SkuLadderEntity;
import cn.edu.zjut.coupon.service.MemberPriceService;
import cn.edu.zjut.coupon.service.SkuFullReductionService;
import cn.edu.zjut.coupon.service.SkuLadderService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("skuFullReductionService")
public class SkuFullReductionServiceImpl extends ServiceImpl<SkuFullReductionDao, SkuFullReductionEntity> implements SkuFullReductionService {

    @Autowired
    private SkuLadderService skuLadderService;

    @Autowired
    private MemberPriceService memberPriceService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuFullReductionEntity> page = this.page(
                new Query<SkuFullReductionEntity>().getPage(params),
                new QueryWrapper<SkuFullReductionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuReduction(SkuReductionTO skuReductionTO) {
        // 7、sku 优惠、满减信息 [gulimall-sms]
        // 7.1、sms_sku_ladder
        SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
        skuLadderEntity.setSkuId(skuReductionTO.getSkuId());
        skuLadderEntity.setFullCount(skuReductionTO.getFullCount());
        skuLadderEntity.setDiscount(skuReductionTO.getDiscount());
        skuLadderEntity.setAddOther(skuReductionTO.getCountStatus());
        if (skuReductionTO.getFullCount() > 0) {
            this.skuLadderService.save(skuLadderEntity);
        }

        // 7.2、sms_sku_full_reduction
        SkuFullReductionEntity reductionEntity = new SkuFullReductionEntity();
        BeanUtils.copyProperties(skuReductionTO, reductionEntity);
        if (reductionEntity.getFullPrice().compareTo(new BigDecimal("0")) > 0) {
            this.save(reductionEntity);
        }


        // 7.3、sms_member_price
        List<MemberPrice> memberPrice = skuReductionTO.getMemberPrice();

        List<MemberPriceEntity> collect = memberPrice.stream().map(item -> {
            MemberPriceEntity priceEntity = new MemberPriceEntity();
            priceEntity.setSkuId(skuReductionTO.getSkuId());
            priceEntity.setMemberLevelId(item.getId());
            priceEntity.setMemberLevelName(item.getName());
            priceEntity.setMemberPrice(item.getPrice());
            priceEntity.setAddOther(1);
            return priceEntity;
        }).filter(item -> { //TODO filter
            return item.getMemberPrice().compareTo(new BigDecimal("0")) > 0;
        }).collect(Collectors.toList());

        this.memberPriceService.saveBatch(collect);
    }

}