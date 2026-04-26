package com.demo.elasticsearch.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * 高亮搜索响应对象
 * 
 * 封装高亮搜索的结果，包含匹配的产品信息和对应的高亮内容
 * 
 * @author Demo
 */
public class HighlightSearchResponse {
    
    /**
     * 查询到的总记录数
     */
    private long total;
    
    /**
     * 当前页码
     */
    private int page;
    
    /**
     * 每页大小
     */
    private int size;
    
    /**
     * 产品搜索结果列表，包含高亮内容
     */
    private List<ProductView> list = new ArrayList<>();

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public List<ProductView> getList() {
        return list;
    }

    public void setList(List<ProductView> list) {
        this.list = list;
    }
}