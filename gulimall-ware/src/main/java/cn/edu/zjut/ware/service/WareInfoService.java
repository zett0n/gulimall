package cn.edu.zjut.ware.service;

import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;

import cn.edu.zjut.common.utils.PageUtils;
import cn.edu.zjut.common.vo.FareVO;
import cn.edu.zjut.ware.entity.WareInfoEntity;

/**
 * 仓库信息
 *
 * @author zett0n
 * @email d673326004@163.com
 * @date 2021-08-24 05:50:17
 */
public interface WareInfoService extends IService<WareInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    FareVO getFare(Long addrId);
}
