package org.demo.springjpademo.dto;

/**
 * JPA 接口投影 (Interface-based Projection)
 *
 * 作用：当实体类 (User) 包含大量字段或关联对象，而业务只需查询其中特定几个字段时，
 * 可以使用投影来避免全表全字段查询（避免 SELECT *），从而大幅提升查询性能。
 * 底层会自动优化 SQL，例如只查询：SELECT u.id, u.email, u.name FROM users u
 *
 * 核心约定与原理：
 * 1. 命名约定：接口中的方法名必须遵循 JavaBean 的 Getter 命名规范，并且严格匹配目标实体类 (User) 中的属性名。
 *    例如：实体里有 `email` 属性，接口中对应就是 `getEmail()`。
 * 2. 动态代理：不需要手动编写实现类，Spring Data JPA 在运行时会自动为这个接口生成代理对象，并将结果集映射进去。
 * 3. 替代方案：在较新的 Spring Boot 版本中，你也可以直接使用 Java Record 来做投影，
 *    如：`public record UserDTO(Long id, String email, String name) {}`
 *
 * 进阶用法 (Open Projections)：
 * 如果需要对字段进行拼接或计算，可以使用 Spring 的 SpEL 表达式：
 * // @Value("#{target.name + ' (' + target.email + ')'}")
 * // String getDetailedName();
 *
 * @author : Tomatos
 * @date : 2026/4/15
 */
public interface UserDTO {

    Long getId();

    String getEmail();

    String getName();
}