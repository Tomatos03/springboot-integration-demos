package com.demo.elasticsearch.constant;

/**
 * Elasticsearch 相关常量定义
 */
public interface EsConstants {

    /**
     * Elasticsearch 模糊查询 (Fuzzy Query) 的 Fuzziness 常量.
     * <p>
     * 在 Elasticsearch 中，模糊查询基于 Levenshtein 编辑距离算法。
     * 出于性能考虑，ES 仅仅支持以下几种可选值（最大编辑距离限制为 2）：0, 1, 2, 或者 "AUTO"。
     * 使用新版 Java API Client 时，官方移除了枚举，直接接收 String，为了避免拼写错误，请使用以下常量。
     */
    interface Fuzziness {
        /**
         * 自动计算编辑距离（最常用、最推荐）。
         * 根据搜索词项的长度自动确定允许的最大编辑距离：
         * - 0~2 个字符：必须完全匹配 (相当于编辑距离 0)
         * - 3~5 个字符：允许 1 次编辑操作 (相当于编辑距离 1)
         * - >5 个字符：允许 2 次编辑操作 (相当于编辑距离 2)
         */
        String AUTO = "AUTO";

        /**
         * 零容错，必须完全匹配（编辑距离 0，等同于精确匹配）
         */
        String ZERO = "0";

        /**
         * 允许最多 1 次单字符编辑操作（插入、删除、替换或交换）（编辑距离 1）
         */
        String ONE = "1";

        /**
         * 允许最多 2 次单字符编辑操作（编辑距离 2）。这是 ES 允许的最大明确指定的编辑距离。
         */
        String TWO = "2";
    }
}