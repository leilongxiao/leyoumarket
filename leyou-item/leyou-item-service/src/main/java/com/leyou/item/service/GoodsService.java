package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.mapper.*;
import com.leyou.item.pojo.Brand;
import com.leyou.item.pojo.SpecParam;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class GoodsService {

    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BrandMapper brandMapper;

    @Autowired
    private CategoryBrandMapper categoryBrandMapper;

    @Autowired
    private SpecificationService specificationService;

    /**
     * goods页根据分页查询Spu
     * @param key
     * @param saleable
     * @param page
     * @param rows
     * @return
     */
    public PageResult<SpuBo> querySpuBoByPage(String key, Boolean saleable, Integer page, Integer rows) {

        Example example = new Example(Spu.class);
        //criteria标准，准则，原则
        Example.Criteria criteria = example.createCriteria();
        // 1.搜索条件,类似
        //select * from tb_spu where title likes "%key%" and saleable={#saleable}
        if (StringUtils.isNotBlank(key) && saleable != null) {
            criteria.andLike("title", "%" + key + "%").andEqualTo("saleable", saleable);
        }


        // 2.分页条件
        PageHelper.startPage(page, rows);//最b

        // 3.执行查询
        List<Spu> spus = this.spuMapper.selectByExample(example);
        PageInfo<Spu> pageInfo = new PageInfo<>(spus);//最重要2行a

        List<SpuBo> spuBos = new ArrayList<>();
        spus.forEach(spu -> {
            SpuBo spuBo = new SpuBo();
            // copy共同属性的值到新的对象
            BeanUtils.copyProperties(spu, spuBo);
            // 查询分类名称
            List<String> names = this.categoryService.queryNamesByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
            spuBo.setCname(StringUtils.join(names, "/"));

            // 查询品牌的名称
            spuBo.setBname(this.brandMapper.selectByPrimaryKey(spu.getBrandId()).getName());

            spuBos.add(spuBo);
        });

        return new PageResult<>(pageInfo.getTotal(), spuBos);

    }

    /**
     * 根据前端输入（这个前端输入是后端选择了分类之后之后给出的一个cid值）查询品牌
     * @param cid
     * @return
     */
    public List<Brand> queryBrandByCategoryID(Long cid) {
        return categoryBrandMapper.queryBrandByCategoryID(cid);
    }

//    /**
//     * 根据cid查询规格参数
//     * @param cid
//     * @return
//     */
//    public List<SpecParam> querySpecParamsByCid(Long cid) {
//        SpecParam specParam = new SpecParam();
//        specParam.setCid(cid);
//        return this.specParamMapper.select(specParam);
//    }

    /**
     * 根据cid查询规格参数
     * @param cid
     * @return
     */
    public List<SpecParam> querySpecParamsByCid(Long cid) {
        return this.specificationService.queryParamsByGid(null,cid,null,null);
    }
}