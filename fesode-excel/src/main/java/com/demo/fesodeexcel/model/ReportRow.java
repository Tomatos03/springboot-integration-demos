package com.demo.fesodeexcel.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 单条报表行数据。
 *
 * <p>字段访问器由 Lombok {@code @Data} 生成；{@code of(...)} 静态工厂对应全参构造器
 * （{@code @AllArgsConstructor(staticName = "of")}）；{@code @NoArgsConstructor}
 * 供 Jackson 反序列化请求体中的行数据使用。
 *
 * @author fesode
 */
@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class ReportRow {

    /** 序号 */
    private Integer seq;

    /** 姓名 */
    private String name;

    /** 部门 */
    private String department;

    /** 日期 */
    private LocalDate date;

    /** 金额 */
    private BigDecimal amount;
}
