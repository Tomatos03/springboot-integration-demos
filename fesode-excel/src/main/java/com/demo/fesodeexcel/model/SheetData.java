package com.demo.fesodeexcel.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 单个 Sheet 的填充内容：模板填充时一个 Sheet 所需的全部数据。
 *
 * <p>填充规则（模板结构、填充顺序等）见 {@link com.demo.fesodeexcel.util.ExcelUtil}
 * 类注释。占位符与数据字段一一对应：
 * <ul>
 *   <li>{@code sheetName}——Sheet 名称（个数决定最终 Excel 的 Sheet 数量）；</li>
 *   <li>{@code variables}（泛型 {@code T}）——填充 {@code {xxx}} 占位符的数据：
 *       传 {@code Map}（键为占位符名，如 {@code {"title": ...}}）或普通 bean
 *       （字段名与占位符名对应）均可；每个 Sheet 一份，互不影响；</li>
 *   <li>{@code rows}（{@code List<R>}）——填充 {@code {.xxx}} 占位符的数据：
 *       元素 {@code R} 为任意 bean（字段名与 {@code {.xxx}} 占位符名对应），
 *       每个元素展开为一行。</li>
 * </ul>
 *
 * @param <T> 填充 {@code {xxx}} 占位符的数据类型（Map 或 bean）
 * @param <R> 填充 {@code {.xxx}} 占位符的列表元素类型
 * @author Tomatos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class SheetData<T, R> {

    /** Sheet 名称 */
    private String sheetName;

    /** 填充 {@code {xxx}} 占位符的数据（Map 或 bean，字段名/键与占位符名对应） */
    private T variables;

    /** 填充 {@code {.xxx}} 占位符的数据（列表，每个元素对应一行） */
    private List<R> rows;
}