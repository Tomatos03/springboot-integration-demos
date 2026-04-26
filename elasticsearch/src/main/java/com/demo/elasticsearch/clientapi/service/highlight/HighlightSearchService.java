package com.demo.elasticsearch.clientapi.service.highlight;

import com.demo.elasticsearch.dto.HighlightSearchRequest;
import com.demo.elasticsearch.dto.HighlightSearchResponse;

import java.io.IOException;

/**
 * 高亮搜索服务接口
 *
 * 定义高亮搜索功能的契约，包括全文搜索高亮显示等功能
 *
 * @author Demo
 */
public interface HighlightSearchService {
    
    /**
     * 执行高亮全文搜索
     * 
     * 使用 Lambda 风格的 Elasticsearch Java Client API 执行全文搜索查询，
     * 并对匹配的关键词进行高亮显示
     * 
     * @param request 高亮搜索请求对象，包含关键词、字段、高亮标签等参数
     * @return 高亮搜索响应对象，包含匹配的产品列表和高亮内容
     * @throws IOException 当 Elasticsearch 操作失败时抛出
     */
    HighlightSearchResponse highlightSearch(HighlightSearchRequest request) throws IOException;
}