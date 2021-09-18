package cn.edu.zjut.search.vo;

import cn.edu.zjut.common.dto.es.SkuEsDTO;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class SearchResult {

    /**
     * 查询到的所有商品信息
     */
    private List<SkuEsDTO> product;

    /**
     * 当前页码
     */
    private Integer pageNum;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 总页码
     */
    private Integer totalPages;


//     private List<Integer> pageNavs;

    /**
     * 当前查询到的结果，所有涉及到的品牌
     */
    private List<BrandVO> brands;


    /**
     * 当前查询到的结果，所有涉及到的所有属性
     */
    private List<AttrVO> attrs;

    /**
     * 当前查询到的结果，所有涉及到的所有分类
     */
    private List<CatalogVO> catalogs;


    //===========================以上是返回给页面的所有信息============================//
//
//
//     /* 面包屑导航数据 */
//     private List<NavVO> navs;
//
//     @Data
//     public static class NavVO {
//         private String navName;
//         private String navValue;
//         private String link;
//     }
//
//
    @Data
    @Accessors(chain = true)
    public static class BrandVO {

        private Long brandId;

        private String brandName;

        private String brandImg;
    }

    @Data
    @Accessors(chain = true)
    public static class AttrVO {

        private Long attrId;

        private String attrName;

        private List<String> attrValue;
    }

    @Data
    @Accessors(chain = true)
    public static class CatalogVO {

        private Long catalogId;

        private String catalogName;
    }
}
