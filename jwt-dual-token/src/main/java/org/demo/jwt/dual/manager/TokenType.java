package org.demo.jwt.dual.manager;

/**
 * JWT 的业务类型。写入 tokenType 声明，签发和校验时都必须显式指定。
 *
 * @author : Tomatos
 * @date : 2026/11/1
 */
public enum TokenType {

    ACCESS("access"),
    REFRESH("refresh");

    private final String value;

    TokenType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
