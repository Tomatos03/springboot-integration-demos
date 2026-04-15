package org.demo.springjpademo.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户实体类
 * 
 * @Entity: 告诉 JPA 这是一个实体类，JPA 会将这个类映射到数据库中的一张表。
 * @Table(name = "users"): 显式指定映射的数据库表名为 "users"。如果不加，默认表名会是类名 "user"（在某些数据库中 user 是保留字，所以显式指定更好）。
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    /**
     * @Id: 声明该字段为数据库表的主键。
     * @GeneratedValue(strategy = GenerationType.IDENTITY): 主键生成策略。
     *   IDENTITY 表示依赖底层数据库的自增列（如 MySQL 的 AUTO_INCREMENT）。
     *   每次插入新数据时，数据库会自动为这个字段分配一个唯一的自增 ID。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * @Column: 配置字段对应的数据库列属性。
     *   nullable = false: 数据库层面不允许为 null（即 NOT NULL）。
     *   unique = true: 数据库层面添加唯一约束，保证邮箱不能重复。
     *   length = 128: 指定 VARCHAR 的长度为 128（默认是 255）。
     */
    @Column(nullable = false, unique = true, length = 128)
    private String email;

    /**
     * 姓名
     * nullable = false 表示姓名不能为空。
     */
    @Column(nullable = false, length = 64)
    private String name;

    /**
     * @OneToMany: 表示一对多关联关系。一个用户 (User) 可以有多个订单 (PurchaseOrder)。
     *   mappedBy = "user": 
     *     非常关键的属性！它表示当前类（User）是关系的“被维护端”。
     *     "user" 指的是另一端（PurchaseOrder 类）中维护关系的那个属性名。
     *     有了 mappedBy，JPA 就知道这里不需要在 user 表中创建外键，外键存在于 PurchaseOrder 表中。
     * 
     *   cascade = CascadeType.ALL: 级联操作策略。
     *     当我们保存/删除一个 User 时，会自动保存/删除他底下的所有关联的 PurchaseOrder。
     * 
     *   orphanRemoval = true: 孤儿删除。
     *     如果把一个 PurchaseOrder 从当前 orders 集合中移除（即该订单不再属于这个用户），
     *     JPA 会自动生成一条 DELETE 语句把它从数据库中真正删掉。
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Default
    private List<PurchaseOrder> orders = new ArrayList<>();

    /**
     * JPA 双向关联关系中的最佳实践：提供便捷的方法来同时维护两端的关系。
     * 
     * 当你向用户的订单列表中添加一个订单时，不仅要把订单加进集合，
     * 还要把订单所属的 user 对象设置为当前用户，这样外键才能正确保存。
     */
    public void addOrder(PurchaseOrder order) {
        if (order == null) {
            return;
        }
        orders.add(order);
        order.setUser(this); // 维护关系的控制端（给订单设置对应的用户）
    }

    /**
     * 从当前用户的订单列表中移除订单，同时切断订单对当前用户的引用。
     */
    public void removeOrder(PurchaseOrder order) {
        if (order == null) {
            return;
        }
        orders.remove(order);
        order.setUser(null); // 解除关联关系，配合 orphanRemoval = true 会触发 DELETE
    }
}