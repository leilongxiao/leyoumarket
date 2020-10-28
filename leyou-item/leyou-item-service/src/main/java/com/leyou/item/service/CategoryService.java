package com.leyou.item.service;

import com.leyou.item.mapper.CategoryMapper;
import com.leyou.item.pojo.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.ls.LSOutput;

import java.util.ArrayList;
import java.util.List;

@Service
public class CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * 根据父id查询子类目
     *
     * @param pid
     * @return
     */
    public List<Category> queryCategoriesByPid(Long pid) {
        Category record = new Category();
        record.setParentId(pid);
        //根据对象中的父id=0来选择
        return this.categoryMapper.select(record);
    }

    /**
     * 根据id列表查询分类名称列表
     *
     * @param ids
     * @return
     */
    public List<String> queryNamesByIds(List<Long> ids) {
        List<Category> list = this.categoryMapper.selectByIdList(ids);
        List<String> names = new ArrayList<>();
        for (Category category : list) {
            names.add(category.getName());
        }

        return names;
        // return list.stream().map(category -> category.getName()).collect(Collectors.toList());
    }

    /**
     * 添加一个节点
     * @param name
     * @param parentId
     * @param isParent
     * @param sort
     */
    public void categoryAdd(String name, Long parentId, Boolean isParent, Integer sort) {
        Category t = new Category();
        t.setName(name);
        t.setParentId(parentId);
        t.setIsParent(isParent);
        t.setSort(sort);
        this.categoryMapper.insert(t);
    }

    /**
     * 修改某个节点
     * @param name
     */
    public void categoryEdit(Long id,String name) {
        Category category = new Category();
        category.setId(id);
        category.setName(name);
        this.categoryMapper.updateByPrimaryKeySelective(category);
    }
    /**
     * 按照id删除某个节点
     * @param id
     */
    public void categoryDelete(Long id) {
        Category category = new Category();
        category.setId(id);
        this.categoryMapper.delete(category);
    }
}
