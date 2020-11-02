package com.leyou.search.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.api.SpecificationApi;
import com.leyou.item.pojo.*;
import com.leyou.search.GoodsRepository;
import com.leyou.search.client.BrandClient;
import com.leyou.search.client.CategoryClient;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.client.SpecificationClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.pojo.SearchRequest;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class SearchService {

    @Autowired
    private BrandClient brandClient;

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SpecificationClient specificationClient;

    //jackson初始化，中庸fastJson jackson gson
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private GoodsRepository goodsRepository;

    public Goods buildGoods(Spu spu) throws IOException {
        Goods goods = new Goods();

        //根据品牌id查询品牌
        Brand brand = this.brandClient.queryBrandById(spu.getBrandId());

        //根据cid1，cid2，cid3查询对应的一个分类
        List<String> names = this.categoryClient.queryNamesByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));

        //根据spuId查询所有的sku
        List<Sku> skus = this.goodsClient.querySkusBySpuId(spu.getId());
        //初始化价格集合
        List<Long> prices = new ArrayList<>();
        //为了减少索引库压力，只放一部分信息到索引库。一个Map就是一个Sku
        //初始化skuMapList，map中key的取值（id，title,image,price）
        List<Map<String, Object>> skuMapList = new ArrayList<>();
        skus.forEach(sku -> {
            prices.add(sku.getPrice());
            Map<String, Object> skuMap = new HashMap<>();
            skuMap.put("id", sku.getId());
            skuMap.put("title", sku.getTitle());
            skuMap.put("image", StringUtils.isBlank(sku.getImages()) ? "" : StringUtils.split(sku.getImages(), ",")[0]);
            skuMap.put("price", sku.getPrice());
            skuMapList.add(skuMap);
        });

        //查询spuDetail，目的 拿到genericSpec SpecialSpec
        SpuDetail spuDetail = this.goodsClient.querySpuDetailBySpuId(spu.getId());
        //两个反序列化，原来是一个json字符串，变成map
        Map<Long, Object> genericSpecMap = MAPPER.readValue(spuDetail.getGenericSpec(), new TypeReference<Map<Long, Object>>() {
        });
        Map<Long, List<Object>> specialSpecMap = MAPPER.readValue(spuDetail.getSpecialSpec(), new TypeReference<Map<Long, List<Object>>>() {
        });

        //查询所有的搜索规格参数
        List<SpecParam> params = this.specificationClient.queryParams(null, spu.getCid3(), null, true);
        //这里和前面不一样，因为前面的是一个id对应多个sku,这里是spu是一对一的关系，不用List来解决覆盖问题。
        Map<String, Object> specs = new HashMap<>();
        //注意：：这里有两种规格参数，通用的和特殊的，前者保存在*tb_spu_detail*中的generic_spec，后者保存在 中的special_spec
        //tb_spec_param中有字段来区分两者，需要先判断，tb_sku中有own_spec
        params.forEach(param -> {
            //判断是否是通用的规格参数
            if (param.getGeneric()) {
                //
                String value = genericSpecMap.get(param.getId()).toString();
                //判断是否是数值类型
                if (param.getNumeric()) {
                    //如果是数值类型的参数，返回范围
                    value = chooseSegment(value, param);
                }
                specs.put(param.getName(), value);
            } else {
                //特殊的规格参数
                List<Object> value = specialSpecMap.get(param.getId());
                specs.put(param.getName(), value);
            }

        });
        //把简单地字段的值赋值goods给对象，id、subTitle、brandId、cid、createTime
        BeanUtils.copyProperties(spu, goods);
        goods.setAll(spu.getTitle() + " " + brand.getName() + " " + StringUtils.join(names, " "));
        goods.setPrice(prices);
        //转换为json
        goods.setSkus(MAPPER.writeValueAsString(skuMapList));
        goods.setSpecs(specs);
        return goods;
    }

    //复制2过来的方法，将时间区域弄出来进行比较，先以，切割，然后以-切割，最后剩下的最大值和最小值，用以比较
    private String chooseSegment(String value, SpecParam p) {
        //弄成Double类型好兼容
        double val = NumberUtils.toDouble(value);
        String result = "其它";
        // 保存数值段
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = NumberUtils.toDouble(segs[0]);
            double end = Double.MAX_VALUE;//这是为了最后有一个-的情况，end为超级大的一个值
            if (segs.length == 2) {
                end = NumberUtils.toDouble(segs[1]);
            }
            // 判断是否在范围内
            if (val >= begin && val < end) {
                if (segs.length == 1) {
                    result = segs[0] + p.getUnit() + "以上";
                } else if (begin == 0) {
                    result = segs[1] + p.getUnit() + "以下";
                } else {
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }


    /**
     * 完成基本查询功能
     *
     * @param request
     * @return
     */
    public PageResult<Goods> search(SearchRequest request) {
        //判断查询条件是否为空
        if (StringUtils.isBlank(request.getKey())) {
            return null;
        }
        ;
        //自定义构建器
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //添加查询条件,因为添加搜索小米手机的时候不能只出现查询手机的消息，所以应该加上.operator(Operator.AND)
        queryBuilder.withQuery(QueryBuilders.matchQuery("all", request.getKey()).operator(Operator.AND));
        //添加分页，行号是从0开始
        queryBuilder.withPageable(PageRequest.of(request.getPage() - 1, request.getSize()));
        //添加结果集过滤：id
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id","subTitle","skus"},null));
        //执行查询获取结果集
        Page<Goods> goodsPage = this.goodsRepository.search(queryBuilder.build());

        //返回分页结果集
        return new PageResult<>(goodsPage.getTotalElements(),goodsPage.getTotalPages(),goodsPage.getContent());
    }
}
