package com.leyou.item.pojo;

import javax.persistence.Table;

/**
 * 商品分类和品牌的中间表,两者是多对多关系
 *
 * @author 自动生成 2020-10-20
 */
@Table(name="tb_category_brand")
public class CategoryBrand {

    /**
     * 商品类目id
     */
    private Long categoryId;

    /**
     * 品牌id
     */
    private Long brandId;

}