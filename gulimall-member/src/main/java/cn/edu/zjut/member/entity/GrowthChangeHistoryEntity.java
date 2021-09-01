package cn.edu.zjut.member.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 成长值变化历史记录
 *
 * @author zett0n
 * @email d673326004@163.com
 * @date 2021-08-24 05:04:00
 */
@Data
@Accessors(chain = true)
@TableName("ums_growth_change_history")
public class GrowthChangeHistoryEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId
    private Long id;
    /**
     * member_id
     */
    private Long memberId;
    /**
     * create_time
     */
    private Date createTime;
    /**
     * 改变的值（正负计数）
     */
    private Integer changeCount;
    /**
     * 备注
     */
    private String note;
    /**
     * 积分来源[0-购物，1-管理员修改]
     */
    private Integer sourceType;

}
