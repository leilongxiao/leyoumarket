package com.leyou.search.pojo;

public class SearchRequest {

    private String key;// 搜索条件

    private Integer page;// 当前页码

    // 这里没有将每页条数弄成一个变量，而是做成一个常量，因为它是一个不可变的。
    private static final Integer DEFAULT_SIZE = 20;// 每页大小，不从页面接收，而是固定大小
    private static final Integer DEFAULT_PAGE = 1;// 默认页

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Integer getPage() {
        if(page == null){
            return DEFAULT_PAGE;
        }
        // 获取页码时做一些校验，不能小于1
        return Math.max(DEFAULT_PAGE, page);
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    //查询获取了多少条
    public Integer getSize() {
        return DEFAULT_SIZE;
    }
}
