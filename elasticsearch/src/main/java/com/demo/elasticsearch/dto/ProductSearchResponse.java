package com.demo.elasticsearch.dto;

import java.util.ArrayList;
import java.util.List;

public class ProductSearchResponse {
    private long total;
    private int page;
    private int size;
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
