package com.demo.elasticsearch.clientapi.controller;

import com.demo.elasticsearch.clientapi.service.highlight.HighlightSearchService;
import com.demo.elasticsearch.dto.ApiResponse;
import com.demo.elasticsearch.dto.HighlightSearchRequest;
import com.demo.elasticsearch.dto.HighlightSearchResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * 高级搜索控制器
 *
 * 暴露 Elasticsearch 高级搜索功能的 REST API 接口
 * 目前实现的功能：
 * - 高亮全文搜索：支持多字段搜索和自定义高亮标签
 *
 * @author Demo
 */
@RestController
@RequestMapping("/api/es/advanced")
public class AdvancedSearchController {

    private final HighlightSearchService highlightSearchService;

    public AdvancedSearchController(HighlightSearchService highlightSearchService) {
        this.highlightSearchService = highlightSearchService;
    }

    /**
     * 执行高亮全文搜索
     *
     * 该接口使用 Elasticsearch Java Client 的 Lambda 风格 API 进行高亮搜索。
     * 支持的功能：
     * 1. 多字段搜索：可在多个字段上进行全文搜索
     * 2. 高亮显示：对匹配的关键词进行 HTML 标签包裹
     * 3. 自定义标签：支持自定义高亮前置和后置标签
     * 4. 灵活的分页：支持自定义返回结果数量
     *
     * 请求示例：
     * {
     *     "keyword": "iPhone",
     *     "fields": ["name", "description"],
     *     "preTags": "<em style='color: red;'>",
     *     "postTags": "</em>",
     *     "size": 20
     * }
     *
     * 响应示例：
     * {
     *     "code": 0,
     *     "msg": "success",
     *     "data": {
     *         "total": 10,
     *         "page": 1,
     *         "size": 10,
     *         "list": [
     *             {
     *                 "id": "1",
     *                 "name": "Apple iPhone 15",
     *                 "description": "...",
     *                 "highlightedName": "Apple <em>iPhone</em> 15",
     *                 "highlightedDescription": "...<em>iPhone</em> is a smartphone...",
     *                 ...
     *             }
     *         ]
     *     }
     * }
     *
     * @param request 高亮搜索请求对象
     *        - keyword (必填): 搜索关键词
     *        - fields (可选): 搜索字段数组，默认搜索 ["name", "description"]
     *        - preTags (可选): 高亮前置标签，默认 "<em>"
     *        - postTags (可选): 高亮后置标签，默认 "</em>"
     *        - size (可选): 返回结果数量，默认 10
     *
     * @return ApiResponse 包含高亮搜索结果的响应对象
     *         - total: 查询匹配的总记录数
     *         - page: 当前页码
     *         - size: 返回的结果数量
     *         - list: 产品视图列表，包含 highlightedName 和 highlightedDescription 字段
     *
     * @throws IOException 当 Elasticsearch 操作失败时抛出异常
     */
    @PostMapping("/highlight-search")
    public ApiResponse<HighlightSearchResponse> highlightSearch(@Valid @RequestBody HighlightSearchRequest request) throws IOException {
        // 调用服务层执行高亮搜索
        // Lambda 风格 API 的具体实现在 HighlightSearchServiceImpl 中
        HighlightSearchResponse response = highlightSearchService.highlightSearch(request);
        return ApiResponse.success(response);
    }
}
