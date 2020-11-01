package com.leyou.search.client;

import com.leyou.LeyouSearchApplication;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.pojo.Spu;
import com.leyou.search.GoodsRepository;
import com.leyou.search.pojo.Goods;
import com.leyou.search.service.SearchService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.test.context.junit4.SpringRunner;

import javax.naming.directory.SearchResult;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SpringBootTest(classes = LeyouSearchApplication.class)
@RunWith(SpringRunner.class)
public class SearchTest {

//    @Autowired
//    private CategoryClient categoryClient;
//
//    @Test
//    public void testQueryCategories() {
//        List<String> names = this.categoryClient.queryNamesByIds(Arrays.asList(1L, 2L, 3L));
//        names.forEach(System.out::println);
//    }

    @Autowired
    private ElasticsearchTemplate template;

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SearchService searchService;

    @Test
    public void test() {
        // 创建索引
        this.template.createIndex(Goods.class);
        // 配置映射;
        this.template.putMapping(Goods.class);

        //分批查询spu
        Integer page = 1;
        int rows = 100;
        do {
            PageResult<SpuBo> result = this.goodsClient.querySpuBoByPage(null, true, page, rows);
            //List<SpuBo>转换成goodsList
            List<SpuBo> items = result.getItems();
            List<Goods> goodsList = items.stream().map(spuBo -> {
                try {
                    return this.searchService.buildGoods(spuBo);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }).collect(Collectors.toList());
            this.goodsRepository.saveAll(goodsList);
            page++;
            rows = items.size();
        } while (rows == 100);


    }
}
