package cn.edu.zjut.coupon.service.impl;

import cn.edu.zjut.common.utils.PageUtils;
import cn.edu.zjut.common.utils.Query;
import cn.edu.zjut.coupon.dao.SeckillSessionDao;
import cn.edu.zjut.coupon.entity.SeckillSessionEntity;
import cn.edu.zjut.coupon.entity.SeckillSkuRelationEntity;
import cn.edu.zjut.coupon.service.SeckillSessionService;
import cn.edu.zjut.coupon.service.SeckillSkuRelationService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("seckillSessionService")
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionDao, SeckillSessionEntity> implements SeckillSessionService {

    @Autowired
    private SeckillSkuRelationService seckillSkuRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SeckillSessionEntity> page = this.page(
                new Query<SeckillSessionEntity>().getPage(params),
                new QueryWrapper<SeckillSessionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SeckillSessionEntity> listInDays(Integer days) {
        QueryWrapper<SeckillSessionEntity> queryWrapper = new QueryWrapper<SeckillSessionEntity>()
                .between("start_time", getStartTime(), getEndTime(days));
        List<SeckillSessionEntity> seckillSessionEntities = this.list(queryWrapper);
        
        return seckillSessionEntities.stream()
                .peek(session -> {
                    List<SeckillSkuRelationEntity> relationEntities = this.seckillSkuRelationService.list(
                            new QueryWrapper<SeckillSkuRelationEntity>().eq("promotion_session_id", session.getId()));
                    session.setRelations(relationEntities);
                })
                .collect(Collectors.toList());
    }

    // 当前天数的 00:00:00
    private String getStartTime() {
        LocalDate now = LocalDate.now();
        LocalDateTime time = now.atTime(LocalTime.MIN);
        return time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    // 当前天数+days-1 23:59:59
    private String getEndTime(Integer days) {
        LocalDate now = LocalDate.now();
        LocalDateTime time = now.plusDays(days - 1).atTime(LocalTime.MAX);
        return time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

}