package org.demo.springjpademo.repository;

import org.demo.springjpademo.dto.UserDTO;
import org.demo.springjpademo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * UserRepository 接口
 *
 * 继承 JpaRepository<T, ID> 后，Spring Data JPA 会在启动时自动为该接口生成代理实现类。
 * 泛型参数说明：
 *   T (User): 表示当前 Repository 操作的实体类。
 *   ID (Long): 表示该实体类主键字段（@Id 标注的字段）的数据类型。
 *
 * 继承后立刻获得了开箱即用的基础 CRUD 方法（如 save, findById, delete, findAll 等），
 * 并且基础方法都已经自带了事务机制（读方法带只读事务，写方法带读写事务）。
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * JPA 派生查询 (Query Derivation)
     *
     * Spring Data JPA 允许仅通过方法名就能自动生成 SQL/JPQL 查询语句，无需手动写 SQL。
     * 命名规则：findBy + 属性名 + 条件关键字
     * 这里解析为：SELECT u FROM User u WHERE u.email = ?
     *
     * @param email 用户邮箱
     * @return 使用 Optional 包装可以优雅地处理查询不到数据时返回 null 的情况，避免 NullPointerException。
     */
    Optional<User> findByEmail(String email);

    /**
     * JPA 投影查询 (Projections)
     *
     * 当只需要查询实体的部分字段，或者需要直接返回 DTO 时，可以使用基于接口或基于类的投影。
     * Spring Data 会根据方法名（findByName 解析为按 name 查询），
     * 并发现返回值不是 User 而是 UserDTO，于是它会在底层自动优化 SQL/JPQL 语句，
     * 只查询 UserDTO 需要的字段，并自动帮你完成从查询集到 DTO 对象的映射装配。
     *
     * @param name 用户名
     * @return 映射后的 UserDTO 对象
     */
    UserDTO findByName(String name);

    /**
     * 查询所有用户并将其投影到 UserDTO 接口。
     * 避免了查询全表和关联表，直接获取需要的字段。
     * 
     * @return 包含所选字段的 UserDTO 列表
     */
    List<UserDTO> findAllProjectedBy();

    /**
     * 自定义删除 (Delete)
     *
     * 除了内置的 delete(entity) 外，可以通过 deleteBy 约定自动生成基于条件的删除 SQL。
     *
     * @param email 用户邮箱
     * @return 成功删除的记录数
     */
    long deleteByEmail(String email);

    /**
     * 复杂更新操作 (Update)
     *
     * 约定方法主要用于查询。遇到 UPDATE/DELETE 时，如果先全查出来再在内存里改，性能很低。
     * 这时必须通过 @Modifying 配合 @Query 手写 JPQL 来执行底层 UPDATE 语句。
     * (注意：调用此方法的地方必须加上 @Transactional 事务注解)
     *
     * @param newName 新名字
     * @param email 目标邮箱
     * @return 受影响的行数
     */
    @Modifying
    @Query("UPDATE User u SET u.name = :newName WHERE u.email = :email")
    int updateNameByEmail(@Param("newName") String newName, @Param("email") String email);

    /**
     * 复杂查询 1：手写 JPQL (@Query)
     *
     * 遇到方法名派生（如 findByNameAndEmailAndAge...）太长，或者要执行特殊集合函数时，
     * 直接用 @Query 编写 JPQL（面向实体的查询语言）。
     * 例如：查询名字包含特定字符，并且关联订单数量大于某个值的用户。
     *
     * @param keyword 名字关键字
     * @param minOrderCount 最小订单数
     * @return 用户列表
     */
    @Query("SELECT u FROM User u WHERE u.name LIKE %:keyword% AND SIZE(u.orders) > :minOrderCount")
    List<User> findComplexUsers(@Param("keyword") String keyword, @Param("minOrderCount") int minOrderCount);

    /**
     * 复杂查询 2：使用原生 SQL (nativeQuery = true)
     *
     * 当业务极其复杂，甚至用到了底层数据库特有函数（比如 MySQL 的 JSON 函数、特殊正则等），
     * JPQL 也无能为力时，可以开启 nativeQuery = true 直接写原生 SQL 语句。
     * 缺点是：这会让你的代码与特定数据库绑定，丢失了 JPA 跨数据库无缝迁移的优势。
     *
     * @return 用户列表
     */
    @Query(value = "SELECT * FROM users WHERE name REGEXP '^[A-Z]'", nativeQuery = true)
    List<User> findUsersWithNativeRegex();
}