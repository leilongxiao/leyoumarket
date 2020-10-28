package cn.itcast.elasticsearch.repository;

import cn.itcast.elasticsearch.pojo.Item;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface ItemRepository extends ElasticsearchRepository<Item,Long> {

    //这里不需要写xml文件来实现，会自动实现，这和mybatis—plus不同。可以直接调用了
    List<Item> findByTitle(String title);

    List<Item> findByPriceBetween(double d1,double d2);
}
