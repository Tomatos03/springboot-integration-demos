package org.demo.springjpademo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 采购订单实体类
 * 
 * @Entity: 告诉 JPA 这是一个实体类，JPA 会将这个类映射到数据库表。
 * @Table(name = "purchase_orders"): 指定映射的数据库表名为 "purchase_orders"。
 * 如果不指定，默认会使用类名 "purchase_order" 作为表名。
 */
@Entity
@Table(name = "purchase_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseOrder {

    /**
     * @Id: 声明该字段为表的主键。
     * @GeneratedValue(strategy = GenerationType.IDENTITY): 主键的生成策略为底层数据库的自增列。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * @ManyToOne: 定义多对一的关联关系（多个订单属于一个用户）。
     *   fetch = FetchType.LAZY: 懒加载。
     *     这是非常关键的性能优化！只有当你真正调用 order.getUser() 的方法去获取用户数据时，
     *     JPA 才会去数据库里执行查询 User 表的 SQL。如果用默认的 EAGER（急加载），
     *     每次查询订单都会自动把用户查出来，极易导致性能问题。
     *   optional = false: 声明这个关联是必须的（即订单不能没有对应的用户），这会在底层做优化。
     * 
     * @JoinColumn: 指定外键列的配置。
     *   name = "user_id": 在 purchase_orders 表中，用来指向 User 表主键的外键列名为 user_id。
     *   nullable = false: 数据库层面规定该外键列不能为 null。
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * @Column: 配置金额列。
     *   precision = 12: 数字的总位数（包含小数）。
     *   scale = 2: 小数点后的位数。即最大支持 9999999999.99。
     * 
     * 注意：对于涉及到钱的字段，Java 中强烈建议使用 BigDecimal 而不是 Double/Float，避免精度丢失。
     */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    /**
     * @Enumerated(EnumType.STRING): 规定枚举类型在数据库中的存储方式。
     *   如果不加这个注解，或者使用 EnumType.ORDINAL（默认），数据库会存入数字（如 0, 1, 2）。
     *   一旦未来枚举项增加或顺序改变，原来存入的数字代表的含义就全乱了（数据灾难）！
     *   使用 EnumType.STRING 则会直接存入枚举的名字（如 "CREATED", "PAID"），更加安全且可读。
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    /**
     * 订单创建时间
     * @Column(nullable = false) 表示数据库列不可为空。
     * JPA 2.2 / Hibernate 5 开始完美原生支持 Java 8 的 java.time 包（LocalDateTime/LocalDate 等）。
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;
}