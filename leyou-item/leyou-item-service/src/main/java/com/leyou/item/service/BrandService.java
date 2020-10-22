package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.mapper.BrandMapper;
import com.leyou.item.mapper.CategoryBrandMapper;
import com.leyou.item.pojo.Brand;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class BrandService {
    @Autowired
    private BrandMapper brandMapper;

    @Autowired
    private CategoryBrandMapper categoryBrandMapper;

    /**
     * 根据前端输入查询品牌分页列表
     *
     * @param key
     * @param page
     * @param rows
     * @param sortBy
     * @param desc
     * @return
     */
    public PageResult<Brand> queryBrandsByPage(String key, Integer page, Integer rows, String sortBy, Boolean desc) {
        //初始化一个example对象
        Example example = new Example(Brand.class);
        Example.Criteria criteria = example.createCriteria();//添加字段

        //添加模糊查询
        if (StringUtils.isNotBlank(key)) {
            criteria.andLike("name", "%" + key + "%").orEqualTo("letter", key);
        }

        //添加分页
        PageHelper.startPage(page, rows);
        //添加排序
        if (StringUtils.isNotBlank(sortBy)) {
            example.setOrderByClause(sortBy + (desc ? " desc" : " asc"));//" desc"前面一定有个空格
        }
        //找到适合的通用mapper方法
        List<Brand> brands = this.brandMapper.selectByExample(example);
        PageInfo<Brand> pageInfo = new PageInfo<>(brands);
        return new PageResult<>(pageInfo.getTotal(), pageInfo.getList());
    }

    /**
     * 新增品牌
     *
     * @param brand
     * @param cids
     */
    @Transactional
    public void saveBrand(Brand brand, List<Long> cids) {//无法单独接收cids，应为是一个json数据，它被包在了里面
        //新增品牌,cids是分类id，下面的id是品牌id
        //只能同时成功
        this.brandMapper.insertSelective(brand);
        cids.forEach(cid -> {
            this.brandMapper.saveCategoryAndBrand(cid, brand.getId());
        });

    }

    /**
     * 根据前端输入（这个前端输入是后端选择了分类之后之后给出的一个cid值）查询品牌
     *
     * @param cid
     * @return
     */
    public List<Brand> queryBrandsByCid(Long cid) {
        return categoryBrandMapper.queryBrandByCategoryID(cid);
    }

}
