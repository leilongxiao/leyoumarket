package com.leyou.item.mapper;


import com.leyou.item.pojo.Category;
import tk.mybatis.mapper.additional.idlist.SelectByIdListMapper;
import tk.mybatis.mapper.common.Mapper;

public interface CategoryMapper extends Mapper<Category>, SelectByIdListMapper<Category, Long> {
/*SelectByIdListMapper<T,pk>
  参数    返回的对应 pojo 字段名
         pk  查询的list字段 的数据类型 LONG  INT等
通俗的讲就是，通过列表，查询并返回实体类，自动遍历列表中的数据
 */
}
