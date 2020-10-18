package com.leyou.item.mapper;

import com.leyou.item.pojo.Brand;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

public interface BrandMapper extends Mapper<Brand> {

    /*
    这个是中间表增加行
     */
    @Insert("insert into tb_category_brand (category_id,brand_id) values(#{cid},#{bid})")
    int saveCategoryAndBrand(@Param("cid") Long cid,@Param("bid") Long bid);
}

