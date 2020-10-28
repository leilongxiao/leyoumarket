package cn.itcast.elasticsearch;

import cn.itcast.elasticsearch.pojo.Item;
import cn.itcast.elasticsearch.repository.ItemRepository;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.metrics.avg.InternalAvg;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

@SpringBootTest
@RunWith(SpringRunner.class)
public class ElasticsearchTest {
    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    private ItemRepository itemRepository;

    /**
     * 创建索引库
     */
    @Test
    public void testCreateIndex() {
        // 创建索引，会根据Item类的@Document注解信息来创建
        elasticsearchTemplate.createIndex(Item.class);
        // 配置映射，会根据Item类中的id、Field等字段来自动完成映射
        elasticsearchTemplate.putMapping(Item.class);
    }

    /**
     * 删除索引
     */
    @Test
    public void testDeleteIndex() {
        elasticsearchTemplate.deleteIndex("heima");
    }

    /**
     * ------------------------------------------------------
     * 修改数据
     */
    @Test
    public void testAdd() {
        //更新也是这个，id不变就是
        Item item = new Item(1L, "小米手机7", " 手机",
                "小米", 3499.00, "http://image.leyou.com/13123.jpg");
        this.itemRepository.save(item);
    }

    //批量增新article
    @Test
    public void testAddList() {
        List<Item> list = new ArrayList<>();
        list.add(new Item(1L, "小米手机7", " 手机", "小米", 3499.00, "http://image.leyou.com/13123.jpg"));
        list.add(new Item(2L, "坚果手机R1", " 手机", "锤子", 3699.00, "http://image.leyou.com/123.jpg"));
        list.add(new Item(3L, "华为META10", " 手机", "华为", 4499.00, "http://image.leyou.com/3.jpg"));
        list.add(new Item(4L, "华为123", " 手机", "华为", 5000.00, "http://image.leyou.com/5.jpg"));
        list.add(new Item(5L, "小米123", " 手机", "华为", 6000.00, "http://image.leyou.com/9.jpg"));
        // 接收对象集合，实现批量新增
        itemRepository.saveAll(list);
    }

    //查找
    @Test
    public void testFind() {
//        Optional<Item> item = this.itemRepository.findById(1L);
//        System.out.println(item.get());
        List<Item> items = this.itemRepository.findByTitle("手机");
//      List<Item> items = this.itemRepository.findByPriceBetween(3499d,3699d);

        items.forEach(System.out::println);
    }

    /**
     * 这里可以有很多种高级查询，如模糊查询、匹配查询等
     *ItemRepository类有关
     */
    @Test
    public void testMatch() {
        MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("title", "手机");
        Iterable<Item> search = this.itemRepository.search(matchQueryBuilder);
        search.forEach(System.out::println);
    }

    /**
     * 重点  为自定义查询
     * NativeSearchQueryBuilder自定义查询的工厂
     * QueryBuilders工具类
     * SortBuilders工具类
     */
    @Test
    public void testNative() {
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        // 词条查询(名字里包含手机的)
//        queryBuilder.withQuery(QueryBuilders.matchQuery("category", "手机"));
        //？加了这句就查不出来

        //分页查询，第二页的两条，注意页码是从0开始的
        queryBuilder.withPageable(PageRequest.of(1, 2));

        //排序
        queryBuilder.withSort(SortBuilders.fieldSort("price").order(SortOrder.DESC));
        // 执行查询，返回分页结果集
        Page<Item> search = this.itemRepository.search(queryBuilder.build());
        System.out.println(search.getTotalPages());
        System.out.println(search.getTotalElements());
        //当前页的记录
        search.forEach(System.out::println);
    }

    /**
     * 聚合查询，add可以多个查询条件，之前是with
     * AggregationBuilders工具类
     * 词条为term
     */
    @Test
    public void testAgg() {
        //构建自定义查询工厂
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //field里面为聚合字段名称，term是词条，brands是聚合名称
        queryBuilder.addAggregation(AggregationBuilders.terms("brands").field("brand"));
        //添加结果集过滤，即不包括任何字段，也就是普通结果集
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{}, null));
        //执行聚合查询，获得聚合结果集
        AggregatedPage<Item> itemsPage = (AggregatedPage) this.itemRepository.search(queryBuilder.build());
        //获取聚合结果集中的聚合对象 ：根据聚合名称获取想要的聚合对象，强转成子类：LongTerms，StringTerms，DoubleTerms
        StringTerms brandAgg = (StringTerms) itemsPage.getAggregation("brands");
        //获取聚合中的桶
        brandAgg.getBuckets().forEach(bucket -> {
            //获取桶中的key
            System.out.println(bucket.getKeyAsString());
            //获取桶中的记录数
            System.out.println(bucket.getDocCount());
        });
    }

    /**
     * 聚合查询的子查询，这个需要去了解下什么是聚合查询
     */
    @Test
    public void testSubAgg() {
        //构建自定义查询工厂
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //field里面为聚合字段名称，term是词条，brands是聚合名称
        queryBuilder.addAggregation(AggregationBuilders.terms("brands").field("brand")
                .subAggregation(AggregationBuilders.avg("price_avg").field("price")));
        //添加结果集过滤，即不包括任何字段，也就是普通结果集
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{}, null));
        //执行聚合查询，获得聚合结果集
        AggregatedPage<Item> itemsPage = (AggregatedPage) this.itemRepository.search(queryBuilder.build());
        //获取聚合结果集中的聚合对象 ：根据聚合名称获取想要的聚合对象，强转成子类：LongTerms，StringTerms，DoubleTerms
        StringTerms brandAgg = (StringTerms) itemsPage.getAggregation("brands");
        //获取聚合中的桶
        brandAgg.getBuckets().forEach(bucket -> {
            //获取桶中的key
            System.out.println(bucket.getKeyAsString());
            //获取桶中的记录数
            System.out.println(bucket.getDocCount());
            //解析子聚合,子聚合结果集转成map结构，key-聚合名称，value-聚合对象
            //Map<price_avg,aggregation>
            Map<String, Aggregation> stringAggregationMap = bucket.getAggregations().asMap();
            InternalAvg price_avg = (InternalAvg) stringAggregationMap.get("price_avg");
            System.out.println(price_avg.getValue());
        });
    }
}




