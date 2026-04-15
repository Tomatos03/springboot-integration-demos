package com.demo.elasticsearch.dto;

public class CategoryBucket {
    private String key;
    private long docCount;

    public CategoryBucket() {
    }

    public CategoryBucket(String key, long docCount) {
        this.key = key;
        this.docCount = docCount;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public long getDocCount() {
        return docCount;
    }

    public void setDocCount(long docCount) {
        this.docCount = docCount;
    }
}
