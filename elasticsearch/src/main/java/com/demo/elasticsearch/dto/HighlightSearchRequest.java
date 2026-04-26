package com.demo.elasticsearch.dto;

/**
 * 高亮搜索请求对象
 * 
 * 用于支持全文搜索时对匹配关键词进行高亮显示
 * 
 * @author Demo
 */
public class HighlightSearchRequest {
    
    /**
     * 搜索关键词（必填）
     */
    private String keyword;
    
    /**
     * 搜索的字段列表，默认搜索 name 和 description 字段
     */
    private String[] fields;
    
    /**
     * 高亮前置标签，默认为 &lt;em&gt;
     */
    private String preTags = "<em>";
    
    /**
     * 高亮后置标签，默认为 &lt;/em&gt;
     */
    private String postTags = "</em>";
    
    /**
     * 分页大小，默认为 10
     */
    private int size = 10;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String[] getFields() {
        return fields;
    }

    public void setFields(String[] fields) {
        this.fields = fields;
    }

    public String getPreTags() {
        return preTags;
    }

    public void setPreTags(String preTags) {
        this.preTags = preTags;
    }

    public String getPostTags() {
        return postTags;
    }

    public void setPostTags(String postTags) {
        this.postTags = postTags;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}