package com.leyou.goods.service;

import com.leyou.goods.client.GoodsClient;
import com.leyou.goods.client.SpecificationClient;
import com.leyou.item.pojo.*;
import com.leyou.goods.client.BrandClient;
import com.leyou.goods.client.CategoryClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.*;

@Service
public class GoodsService {
    @Autowired
    private BrandClient brandClient;

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SpecificationClient specificationClient;

    @Autowired
    private GoodsService goodsService;

    @GetMapping("item/{spuId}.html")
    public String toImagePage(Model model){

        return "item";
    }

    public Map<String, Object> loadData(Long spuId) {
        Map<String, Object> model = new HashMap<>();

        // 根据id查询spu对象
        Spu spu = this.goodsClient.querySpuById(spuId);

        // 查询spudetail
        SpuDetail spuDetail = this.goodsClient.querySpuDetailBySpuId(spuId);

        // 查询sku集合
        List<Sku> skus = this.goodsClient.querySkusBySpuId(spuId);

        // 查询分类
        List<Long> cids = Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3());
        List<String> names = this.categoryClient.queryNamesByIds(cids);
        List<Map<String, Object>> categories = new ArrayList<>();
        for (int i = 0; i < cids.size(); i++) {
            Map<String, Object> categoryMap = new HashMap<>();
            categoryMap.put("id", cids.get(i));
            categoryMap.put("name", names.get(i));
            categories.add(categoryMap);
        }

        // 查询品牌
        Brand brand = this.brandClient.queryBrandById(spu.getBrandId());

        // 查询规格参数组
        List<SpecGroup> groups = this.specificationClient.queryGroupWithParam(spu.getCid3());

        // 查询特殊的规格参数
        List<SpecParam> params = this.specificationClient.queryParams(null, spu.getCid3(), null, false);
        Map<Long, String> paramMap = new HashMap<>();
        params.forEach(param -> {
            paramMap.put(param.getId(), param.getName());
        });

        // 分类
        model.put("categories", categories);
        // 封装spu
        model.put("spu", spu);
        // 封装spuDetail
        model.put("spuDetail", spuDetail);
        // 封装sku集合
        model.put("skus", skus);
        // 品牌
        model.put("brand", brand);
        // 规格参数组
        model.put("groups", groups);
        // 查询特殊规格参数
        model.put("paramMap", paramMap);
        return model;
    }
}
