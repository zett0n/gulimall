package cn.edu.zjut.search.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 封装查询条件
 */
@Data
@Accessors(chain = true)
public class SearchParam {

    /**
     * 页面传递过来的全文匹配关键字
     */
    private String keyword;

    /**
     * 品牌id,可以多选
     */
    private List<Long> brandId;

    /**
     * 三级分类id
     */
    private Long catalog3Id;

    /**
     * 排序条件：sort=price/salecount/hotscore_desc/asc
     */
    private String sort;

    /**
     * 是否显示有货 0/1
     */
    private Integer hasStock;

    /**
     * 价格区间查询 _500 / 100_500 / 500_
     */
    private String skuPrice;

    /**
     * 按照属性进行筛选 1_5寸:6寸:7寸
     */
    private List<String> attrs;

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 原生的所有查询条件
     */
    private String _queryString;

}
