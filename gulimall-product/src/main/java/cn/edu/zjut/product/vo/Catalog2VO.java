package cn.edu.zjut.product.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class Catalog2VO {
    /**
     * 一级父分类的id
     */
    private String catalog1Id;

    /**
     * 三级子分类
     */
    private List<Catalog3VO> catalog3List;

    private String id;

    private String name;

    /**
     * 三级分类vo
     */
    @Data
    @Accessors(chain = true)
    public static class Catalog3VO {

        /**
         * 父分类、二级分类id
         */
        private String catalog2Id;

        private String id;

        private String name;
    }
}
