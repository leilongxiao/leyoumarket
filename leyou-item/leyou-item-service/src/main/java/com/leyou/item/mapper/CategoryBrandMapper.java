package com.leyou.item.mapper;

import com.leyou.item.pojo.Brand;
import com.leyou.item.pojo.CategoryBrand;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface CategoryBrandMapper extends Mapper<CategoryBrand> {

    /*查出品牌名字*/
    @Select("select tb.*\n" +
            "from tb_category_brand tcb\n" +
            "left join tb_brand tb\n" +
            "on tcb.brand_id=tb.id\n" +
            "where category_id=#{cid}")
    List<Brand> queryBrandByCategoryID(Integer cid);
}
