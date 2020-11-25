package com.leyou.goods.client;

import com.leyou.item.api.GoodsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "item-service")
public interface GoodsClient extends GoodsApi {

//    不用这个
//    /*
//    一批一批查询，这里是分页查询，直接从GoodsService中调用方法就可以
//     */
//    @GetMapping("spu/page")
//    public PageResult<SpuBo> querySpuBoByPage(
//            @RequestParam(value = "key", required = false) String key,
//            @RequestParam(value = "saleable", required = false) Boolean saleable,
//            @RequestParam(value = "page", defaultValue = "1") Integer page,
//            @RequestParam(value = "rows", defaultValue = "5") Integer rows
//    );
}
