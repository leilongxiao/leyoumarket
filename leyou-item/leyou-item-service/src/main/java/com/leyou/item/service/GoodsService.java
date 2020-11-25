package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.pojo.*;
import com.leyou.item.mapper.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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
    private SpecParamMapper specParamMapper;

    @Autowired
    private SpuDetailMapper spuDetailMapper;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;

    /**
     * goods页根据分页查询Spu
     *
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

        if (StringUtils.isNotBlank(key)) {
            criteria.andLike("title", "%" + key + "%");
        }

        if (saleable != null) {
            criteria.andEqualTo("saleable", saleable);
        }
        //这两个if必须分开，从逻辑上看的
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
     * 根据cid查询规格参数
     *
     * @param cid
     * @return
     */
    public List<SpecParam> querySpecParamsByCid(Long cid) {
        SpecParam specParam = new SpecParam();
        specParam.setCid(cid);
        return this.specParamMapper.select(specParam);
    }


    /**
     * 新增商品
     *
     * @param spuBo
     */
    @Transactional
    public void saveGoods(SpuBo spuBo) {
        // 新增spu
        // 设置默认字段
//      spuBo.setId(null);
        spuBo.setSaleable(true);
        spuBo.setValid(true);
        spuBo.setCreateTime(new Date());
        spuBo.setLastUpdateTime(spuBo.getCreateTime());
        this.spuMapper.insertSelective(spuBo);

        // 新增spuDetail
        SpuDetail spuDetail = spuBo.getSpuDetail();
        spuDetail.setSpuId(spuBo.getId());
        this.spuDetailMapper.insertSelective(spuDetail);

//        List<Sku> skus = spuBo.getSkus();
//        skus.forEach(sku -> sku.setCreateTime(new Date()));
        addSkusAndStock(spuBo);
        //this.skuMapper.insertList(skus);

        //只有增删改需要amqp
        sendMsg("insert",spuBo.getId());
    }

    private void sendMsg(String type,Long spuId) {
        try {
            this.amqpTemplate.convertAndSend("item."+type, spuId);
        } catch (AmqpException e) {
            e.printStackTrace();
        }
    }

    //增加skus-stock的方法，这个方法只是被别的方法调用，本身不直接被controller调用
    public void addSkusAndStock(SpuBo spuBo) {
        List<Sku> skus = spuBo.getSkus();
        skus.forEach(sku -> {
            //新增sku
            sku.setId(null);
            //在SpuBo方法中为id
            sku.setSpuId(spuBo.getId());
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());
            this.skuMapper.insertSelective(sku);
            //新增stock,先设置属性名称不对应的属性，然后插入数据库
            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());
            this.stockMapper.insertSelective(stock);
        });
    }

    public SpuDetail querySpuDetailBySpuId(Long spuId) {
        return this.spuDetailMapper.selectByPrimaryKey(spuId);
    }

    public List<Sku> querySkusBySpuId(Long spuId) {
        Sku sku = new Sku();
        sku.setSpuId(spuId);
        List<Sku> skus = this.skuMapper.select(sku);
        //给skus加上 库存（没考虑到的），应该表和对象连起来看
        skus.forEach(s -> {
            Stock stock = this.stockMapper.selectByPrimaryKey(s.getId());
            s.setStock(stock.getStock());
        });
        return skus;
    }

    /**
     * 更新货物
     *
     * @param spuBo
     */
    @Transactional
    public void updateGoods(SpuBo spuBo) {
        //1.获取旧数据对象（数据库中的spu和spuDetail,sku和stock）
        //2.设置新数据
        //3.保存更新update

        //多个sku-stock是特色，条数可能变化，需要先删除后添加
        this.deleteSkusAndStock(spuBo.getId());
        //添加sku-stock
        this.addSkusAndStock(spuBo);

        spuBo.setLastUpdateTime(new Date());
        spuBo.setCreateTime(null);
        spuBo.setValid(true);//因为可以看到等，就是为true的，不能改完之后就看不到了这样子
        spuBo.setSaleable(true);

        //spu和spuDetail更新即可
        this.spuMapper.updateByPrimaryKeySelective(spuBo);//
        this.spuDetailMapper.updateByPrimaryKeySelective(spuBo.getSpuDetail());

        sendMsg("update",spuBo.getId());
    }

    /**
     * 删除sku-stock
     */
    public void deleteSkusAndStock(Long spuId) {
        List<Sku> skus = this.querySkusBySpuId(spuId);
        if (!CollectionUtils.isEmpty(skus)) {
            skus.forEach(sku -> {
                this.skuMapper.delete(sku);
                this.stockMapper.deleteByPrimaryKey(sku.getId());
            });
        }
    }

    /**
     * 必须只能删除sku表
     *
     * @param spuId
     */
    public void deleteGood(Long spuId) {
        //根据spuId伤处skus和stock
        this.deleteSkusAndStock(spuId);
        //删除spu表和spuDetail中对应的spuId行
        this.spuDetailMapper.deleteByPrimaryKey(spuId);
        this.spuMapper.deleteByPrimaryKey(spuId);

        sendMsg("delete",spuId);
    }

    /**
     * 上/下架转换
     * @param spuId
     */
    public void revertSaleableById(Long spuId) {
        Spu spu1 = new Spu();
        Spu spu = this.spuMapper.selectByPrimaryKey(spuId);
        spu1.setId(spuId);
        spu1.setSaleable(!spu.getSaleable());
        this.spuMapper.updateByPrimaryKeySelective(spu1);
    }


    public Spu queryspuBySpuId(Long spuId) {
        return this.spuMapper.selectByPrimaryKey(spuId);
    }
}
