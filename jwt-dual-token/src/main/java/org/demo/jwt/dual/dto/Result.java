package org.demo.jwt.dual.dto;

/**
 * 统一响应结构：业务状态码、业务信息、数据。
 *
 * <p>成功状态码与描述是响应结构自身的属性，直接定义在本类中；失败码见 {@link BizCode}。</p>
 *
 * <p>成功示例：</p>
 * <pre>{@code
 * {"code": 0, "message": "success", "data": {...}}
 * }</pre>
 *
 * <p>失败示例：</p>
 * <pre>{@code
 * {"code": 40103, "message": "access token 已过期", "data": null}
 * }</pre>
 *
 * @param code    业务状态码，成功为 {@link #SUCCESS_CODE}，失败见 {@link BizCode}
 * @param message 业务信息
 * @param data    业务数据，失败时为 null
 * @author : Tomatos
 * @date : 2026/11/1
 */
public record Result<T>(int code, String message, T data) {

    /** 成功业务状态码。 */
    public static final int SUCCESS_CODE = 0;

    /** 成功业务描述。 */
    public static final String SUCCESS_MESSAGE = "success";

    public static <T> Result<T> success(T data) {
        return new Result<>(SUCCESS_CODE, SUCCESS_MESSAGE, data);
    }

    public static <T> Result<T> success(String message, T data) {
        return new Result<>(SUCCESS_CODE, message, data);
    }

    public static <T> Result<T> failure(BizCode bizCode) {
        return new Result<>(bizCode.code(), bizCode.message(), null);
    }

    public static <T> Result<T> failure(BizCode bizCode, String message) {
        return new Result<>(bizCode.code(), message, null);
    }
}
