package org.demo.springjpademo.repository;

import org.demo.springjpademo.entity.OrderStatus;
import org.demo.springjpademo.entity.PurchaseOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

/**
 * 采购订单的 Repository 接口
 * 
 * 继承 JpaRepository<PurchaseOrder, Long>，即可获取所有标准的 CRUD 操作支持，
 * Spring Data JPA 会在运行时自动生成这个接口的代理实现类。
 */
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    /**
     * JPA 派生查询 (Query Derivation)
     * 
     * Spring Data JPA 提供了通过方法名自动生成 SQL 查询的能力。
     * 命名规则通常为：findBy + 属性1 + 条件关键字 + And/Or + 属性2 + 条件关键字
     * 这里的解析逻辑为：查询 status 等于给定状态，且 amount 大于 (GreaterThan) 给定金额阈值的所有订单。
     * 底层自动生成的原生 SQL 类似于：
     *   SELECT * FROM purchase_orders WHERE status = ? AND amount > ?
     * 
     * @param status 订单状态枚举
     * @param amount 金额阈值
     * @return 满足条件的订单实体列表
     */
    List<PurchaseOrder> findByStatusAndAmountGreaterThan(OrderStatus status, BigDecimal amount);

    /**
     * 分页查询 (Pagination)
     * 
     * 当方法参数中包含 Pageable 时，Spring Data JPA 会自动在执行时应用分页和排序逻辑。
     * 同时，它会额外执行一条 COUNT 语句来统计满足条件的总记录数。
     * 返回的 Page<T> 包装对象中包含了：当前页的数据列表 (content)、总记录数 (totalElements)、总页数 (totalPages) 等完整分页信息。
     * 
     * 该方法的解析逻辑为：按指定的 status 查出所有订单，并返回分页结果。
     * 
     * @param status 订单状态枚举
     * @param pageable 分页与排序的配置对象（由调用方如 Controller 或 Service 构建并传入）
     * @return 包含分页信息和当前页数据的 Page 包装对象
     */
    Page<PurchaseOrder> findByStatus(OrderStatus status, Pageable pageable);

    /**
     * JPQL 自定义查询 (@Query)
     * 
     * 当派生查询的方法名过长，或涉及多表/复杂业务逻辑时，可以使用 @Query 注解手动编写查询语句。
     * JPQL (Java Persistence Query Language) 语法类似 SQL，但它是【面向对象】的。
     * 
     * 特点说明：
     * 1. select o from PurchaseOrder o：查询目标是实体类（PurchaseOrder），而不是具体的数据库表。
     * 2. o.user.email：JPQL 允许顺着对象属性进行"导航"。因为 PurchaseOrder 实体中有 user 属性，所以可以直接通过 o.user 导航到 User 实体，再查 email。
     *    底层的 Hibernate 会自动将其翻译为涉及 purchase_orders 表与 users 表的 JOIN 语句。
     * 3. :email：表示这是一个命名参数，在运行时它会被替换为传入的方法参数 email。
     * 
     * @param email 用户邮箱
     * @return 属于该邮箱用户的所有订单列表
     */
    @Query("select o from PurchaseOrder o where o.user.email = :email")
    List<PurchaseOrder> findAllByUserEmail(String email);
}