# spring-jpa-demo

这是一个用于学习 **Spring Data JPA** 的最小可运行 Demo，包含实体映射、关联关系、Repository 查询、事务、分页排序、初始化数据等核心能力。

## 模块结构

```text
spring-jpa-demo
├── pom.xml
├── README.md
└── src/main
    ├── java/org/demo/springjpademo
    │   ├── SpringJpaDemoApplication.java
    │   ├── config/DataInitializer.java
    │   ├── controller/OrderController.java
    │   ├── entity
    │   │   ├── OrderStatus.java
    │   │   ├── PurchaseOrder.java
    │   │   └── User.java
    │   ├── repository
    │   │   ├── PurchaseOrderRepository.java
    │   │   └── UserRepository.java
    │   └── service/OrderService.java
    └── resources/application.yml
```

## 快速开始

1. **构建模块**  
   在仓库根目录执行以下命令进行编译打包（会自动处理依赖模块）：
   ```bash
   mvn -pl spring-jpa-demo -am clean package
   ```

2. **运行模块**  
   运行 Spring Boot 应用：
   ```bash
   mvn -pl spring-jpa-demo spring-boot:run
   ```
   启动后默认端口为：`8080`

3. **访问 H2 内存数据库控制台**  
   - 浏览器打开地址：`http://localhost:8080/h2-console`
   - JDBC URL 填入：`jdbc:h2:mem:jpa_demo;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE`
   - 用户名：`sa`
   - 密码：（留空即可）
   - 点击 **Connect** 即可查看运行时生成的表与初始化数据。

4. **测试示例接口**  
   基础路径为 `/api/orders`：
   - 创建订单：`POST http://localhost:8080/api/orders?userId=1&amount=88.50`
   - 支付订单：`PUT http://localhost:8080/api/orders/1/pay`
   - 按状态分页查询：`GET http://localhost:8080/api/orders?status=PAID&page=0&size=10`

## 注解讲解

本项目中使用了 Spring Data JPA 的诸多核心注解，以下是它们的基础作用说明：

### 1. 实体映射注解
- `@Entity`：表明该类是一个 JPA 实体类，将被映射到数据库中的一张表。
- `@Table(name = "xxx")`：指定实体对应的数据库表名（如果不写，通常默认映射为首字母小写的类名）。
- `@Id`：标记当前字段为数据库表的主键。
- `@GeneratedValue(strategy = GenerationType.IDENTITY)`：指定主键的生成策略。`IDENTITY` 表示使用数据库底层的自增列（如 MySQL 的 `AUTO_INCREMENT`）。
  
  **常见的 `GenerationType` 策略选项说明：**
  
  | 策略类型 | 说明 | 适用数据库场景 |
  | :--- | :--- | :--- |
  | `IDENTITY` | 依赖底层数据库的自增主键列（Auto Increment），在插入时由数据库直接生成。 | MySQL, SQL Server, PostgreSQL(较新版本) 等 |
  | `SEQUENCE` | 使用数据库底层的序列对象（Sequence）来生成主键。可通过 `@SequenceGenerator` 详细配置序列名称和步长。 | Oracle, PostgreSQL 等支持序列的数据库 |
  | `TABLE` | 框架自动生成一张特定的表来维护主键的当前值。为了保证唯一性会涉及行级锁，性能相对较低。 | 所有的数据库均可使用（作为通用备用方案） |
  | `AUTO` | （默认策略）由 JPA 厂商（如 Hibernate）根据当前连接的数据库方言（Dialect）自动在上述三种策略中选择最合适的一种。 | 跨多种数据库的项目 |
- `@Column`：用于配置字段级的映射细节。例如 `nullable = false` (非空)，`unique = true` (唯一约束)，`length = 128` (字段最大长度)，`precision` 和 `scale` (用于高精度数字类型的位宽配置)。
- `@Enumerated(EnumType.STRING)`：指定枚举字段在数据库中存储为字符串形式（而非默认的数字索引），这可以保证数据的可读性，并且避免将来枚举项顺序变动导致的数据错位。
  
  **`EnumType` 提供的两种映射策略：**
  
  | 策略类型 | 数据库存储形式 | 优点 | 缺点 |
  | :--- | :--- | :--- | :--- |
  | `STRING` | 字符串（如 "CREATED"） | 数据可读性强；重排枚举项顺序不会导致旧数据含义错位。 | 相比数字占用稍多存储空间；字符串查询性能略低于数字索引。 |
  | `ORDINAL` | 整数索引（0, 1, 2...） | （默认策略）存储最紧凑，理论查询和比较性能最好。 | 极易引发数据灾难！如果后期在枚举中间插入新项或调整了顺序，数据库里的旧数字代表的含义就会全乱掉，生产中通常禁用。 |

### 2. 关联关系与配置注解

#### 关联关系注解

定义

- `@OneToOne`：定义一对一关系（如用户与用户详情）。
- `@OneToMany`：定义一对多关系（如一个用户拥有多个订单）。本 Demo 中的 `User` 使用了此注解。

- `@ManyToOne`：定义多对一关系。本 Demo 中的 `PurchaseOrder` 使用了此注解指向 `User`。
- `@ManyToMany`：定义多对多关系（如学生与课程）。通常需要配合中间表映射。
  
  | 属性 | 作用说明 |
  | :--- | :--- |
  | `mappedBy` | 放置在“关系被维护端”（即一的一方，如 `User`），指明该关联关系是由另一端的某个属性（如 `user`）来维护外键关系，放弃当前端的外键维护权。 |
  | `cascade` | 级联操作策略。例如配置 `CascadeType.ALL`，当对主对象（`User`）进行保存、删除等操作时，会自动级联到其关联的所有子对象（`PurchaseOrder`）。 |
  | `orphanRemoval` | 孤儿删除。设为 `true` 时，若子对象从父对象的集合中被主动移除（彻底解除引用），JPA 会自动将其从数据库中执行 DELETE 删除操作。 |

#### 关联配置注解（配置细节）
- `@JoinColumn`：指定外键列名称，通常放置在“关系维护端”（多的一方，如 `PurchaseOrder` 中的 `user_id` 列）。
- `@JoinTable`：指定中间表名称和字段，通常用于 `@ManyToMany` 或不需要外键列的单向 `@OneToMany` 关系中，用于配置中间表的表名及两端的外键列名。
- `@JoinColumnOrFormula`：外键或公式（高级用法）。这是 Hibernate 扩展注解，允许你在使用常规列关联的同时，也能使用一段 SQL 公式作为关联条件，用于处理非常规的遗留表结构。

### 3. 数据访问与事务注解
- `@Query`：允许在 Repository 接口方法上直接编写自定义的 JPQL（Java Persistence Query Language）或原生 SQL 语句，用于执行复杂查询。
- `@Transactional`：声明事务边界。加在 Service 层方法上时，如果方法抛出 `RuntimeException`，则会自动回滚数据库的所有写操作。配置 `@Transactional(readOnly = true)` 可用于纯查询的方法，能够帮助 Hibernate 进行底层优化并避免不必要的脏数据检查（Dirty Checking）。

## 配置约定

Spring Data JPA 和 Spring Boot 底层提供了大量“约定优于配置”的默认行为，了解这些约定能帮助你大幅减少重复配置代码：

### 1. 命名策略约定 (Naming Strategy)
- **表名映射**：如果不使用 `@Table(name="...")`，实体类 `PurchaseOrder` 会默认映射为表名 `purchase_order`（驼峰转下划线）。
- **列名映射**：如果不使用 `@Column(name="...")`，实体类中的驼峰命名属性（如 `createdAt`）会默认映射为下划线小写列名（`created_at`）。Spring Boot 默认使用了 `CamelCaseToUnderscoresNamingStrategy` 来实现这种自动转换。

### 2. DDL 自动生成约定
- 当引入了内嵌数据库（如 H2、HSQL）时，Spring Boot 默认会将 `spring.jpa.hibernate.ddl-auto` 设置为 `create-drop`（每次启动创建表，关闭应用时销毁）。
- 为了保留数据或演示，本 Demo 在 `application.yml` 中显式指定为 `update`（表不存在则创建，实体有新增字段则修改表结构，但不删除原有数据列）。

### 3. Repository 接口约定
- **自动实现**：只需要定义接口并继承 `JpaRepository<T, ID>`，Spring Data 就会在启动时通过动态代理自动生成对应的实现类，提供几十种常见的开箱即用的 CRUD 与分页方法。
  > [!TIP]
  > **`JpaRepository<T, ID>` 泛型参数说明：**
  >
  > - `T` (Type)：当前 Repository 负责管理的业务**实体类型**，比如 `User`。
  > - `ID` (Identity)：该实体类的主键 `@Id` 字段的**数据类型**，比如 `Long`。
- **默认事务机制**：`JpaRepository` 内置的默认方法已经自带了事务。读方法（如 `findById`）默认是 `@Transactional(readOnly = true)`，写方法（如 `save`、`delete`）默认开启了普通事务。因此基础的单表 DB 操作即使不在 Service 层加 `@Transactional` 也能安全运行。

### 4. 方法名派生查询约定 (Query Derivation)
- Repository 接口的方法名只要遵循 `findBy` / `countBy` / `deleteBy` + `属性名` + `条件关键字`（如 `And`, `GreaterThan`, `In`）的特定命名模式，Spring Data JPA 会自动在解析时生成对应的 SQL 语句。


**JPA Repository 方法名称分类映射表**

| 分类 | 词汇 | 作用 | 示例 |
| :--- | :--- | :--- | :--- |
| **操作前缀** | `find`, `read`, `get`, `query`, `count`, `exists`, `delete`, `remove` | 定义操作类型 | `findBy...`, `countBy...` |
| **范围限定** | `All`, `One`, `First`, `Top`, `Distinct` | 限制结果集 | `findAllBy...`, `findFirstBy...` |
| **条件连接** | `By` | 分隔符，区分操作与条件 | `findAllBy`, `findByName` |
| **条件操作** | `And`, `Or`, `Like`, `Contains`, `GreaterThan`, `LessThan`, `Between`, `In`, `IgnoreCase`, `OrderBy` 等 | 构建 WHERE 子句 | `findByNameAndStatus`, `findByAgeGreaterThan` |

**组合关键字示例 **

在实际业务中，我们通常会使用 `And`、`Or` 等逻辑词把单关键字组合成极长的复杂条件。以下示例展示了各类型关键字的组合以及**完整的底层 SQL**：

| 组合方法示例 | 包含的关键字 | 完整的等价 SQL |
| :--- | :--- | :--- |
| `findByNameAndEmail(...)` | `And` | `SELECT * FROM users WHERE name = ? AND email = ?` |
| `findByNameOrEmail(...)` | `Or` | `SELECT * FROM users WHERE name = ? OR email = ?` |
| `findByAgeGreaterThanEqualAndAgeLessThan(...)` | `GreaterThanEqual`, `LessThan` | `SELECT * FROM users WHERE age >= ? AND age < ?` |
| `findByAmountBetweenOrderByCreatedAtDesc(...)` | `Between`, `OrderBy` | `SELECT * FROM purchase_orders WHERE amount BETWEEN ? AND ? ORDER BY created_at DESC` |
| `findByNameStartingWithAndEmailEndingWith(...)`| `StartingWith`, `EndingWith` | `SELECT * FROM users WHERE name LIKE ?% AND email LIKE %?` |
| `findByTitleContainingAndStatusNot(...)` | `Containing`, `Not` | `SELECT * FROM posts WHERE title LIKE %?% AND status != ?` |
| `findByStatusInAndDescriptionIsNull(...)` | `In`, `IsNull` | `SELECT * FROM purchase_orders WHERE status IN (?, ?, ...) AND description IS NULL` |
| `findByRoleNotInAndNameIsNotNull(...)` | `NotIn`, `IsNotNull` | `SELECT * FROM users WHERE role NOT IN (?, ?, ...) AND name IS NOT NULL` |
| `findByActiveTrueAndLockedFalse(...)` | `True`, `False` | `SELECT * FROM users WHERE active = true AND locked = false` |
| `countByRoleAndAgeGreaterThan(...)` | `CountBy`, `GreaterThan` | `SELECT COUNT(*) FROM users WHERE role = ? AND age > ?` |
| `existsByEmailAndActiveTrue(...)` | `ExistsBy`, `True` | `SELECT EXISTS(SELECT 1 FROM users WHERE email = ? AND active = true)` |
| `deleteByStatusAndCreatedAtLessThanEqual(...)` | `DeleteBy`, `LessThanEqual`| `DELETE FROM purchase_orders WHERE status = ? AND created_at <= ?` |

> [!TIP]
> ** 在 `By` 之前插入其他无意义的词汇通常也是被允许的。例如 `findUserById`、`querySomethingByName`、`readInfoByEmail` 都是合法的。JPA 会自动忽略 `find` 和 `By` 之间（即 `User`、`Something`、`Info` 等）的内容，只解析 `By` 后面的条件字段。

> [!NOTE] 
> 
> JPA **不支持**单纯通过方法名（如 `updateNameByEmail`）来自动派生 UPDATE 语句。更新操作在 JPA 中通常通过获取实体对象、修改属性并调用 `save()` 来完成（传入对象id是否为空来判断是否为更新操作）。

> [!NOTE] 
> **如果 JPA 方法名不规范（写错了）会怎样？**  
> 答：**启动时报错，应用无法启动。**（这属于“Fail-Fast”机制，在部署前就阻断错误）。

### 5. 按需查询部分字段的约定 (DTO 投影)
如果你有一个包含几十个字段的实体表，但某个场景只需要查询其中的 2~3 个字段，默认的查询（相当于执行 `SELECT *`）会浪费数据库性能和内存。
JPA 提供了一个非常聪明的“按需查询”约定来解决这个问题：

- **你不需要手写 SQL**：只需要新建一个单纯的接口（比如叫 `UserDTO`），在里面只写你需要的那几个字段的 Getter 方法（如 `getId()` 和 `getName()`）。
- **自动优化与装配**：在 Repository 里直接让查询方法返回这个 `UserDTO` 接口。Spring Data JPA 发现你的返回值不是完整的实体类，就会在底层自动生成一条极致优化的 SQL（例如只查两列：`SELECT u.id, u.name FROM users u`），并且自动把查询结果装配成这个接口的样子返回给你。

这种“掐头去尾、只查所需字段”的智能机制，在 JPA 中有一个对应的专业术语叫做 **投影 (Projections)**。

##  JPQL 

JPQL全称**Java Persistence Query Language** ，当你遇到的业务场景极其复杂，或者查询条件非常多导致“方法名派生查询”变得像一列火车一样长（比如 `findByNameAndStatusAndCreatedAtGreaterThan...`）时，就需要使用 `@Query` 注解来手写查询语句了。

默认情况下，`@Query` 中编写的语言叫做 **JPQL**。

**JPQL 与原生 SQL 的核心区别：**

- **SQL 是面向关系的**：你操作的是数据库的**表名**和**列名**。
- **JPQL 是面向对象的**：你操作的是 Java 的**实体类名**和**属性名**。

### **基础查询**

- **SQL**: `SELECT * FROM purchase_orders WHERE amount > 100`
- **JPQL**: `SELECT o FROM PurchaseOrder o WHERE o.amount > 100`
*(注意：`PurchaseOrder` 是 Java 实体类名，`o` 是对象的别名，查询返回的是完整的对象 `o`)*‘

> [!NOTE]
>
> 对于复杂查询（带条件的查询）必须定义别名

> [!TIP]
> **何时该退回使用原生 SQL？**
> 如果 JPQL 依然无法满足你的需求（例如你需要调用 MySQL 特有的 JSON 解析函数、复杂的窗口函数等），可以通过设置 `nativeQuery = true` 退回到原生 SQL 模式：
> `@Query(value = "SELECT * FROM users WHERE ...", nativeQuery = true)`
> 
> **代价**：一旦开启原生 SQL，你的代码就与特定的数据库深度绑定了。而使用 JPQL 时，无论底层切成 MySQL、PostgreSQL 还是 Oracle，Hibernate 都会自动将 JPQL 翻译成对应数据库的方言。

### **对象导航**
在 SQL 中，如果我们要通过订单查对应用户的邮箱，必须写繁琐的 `JOIN` 和 `ON` 条件：

- **SQL**: `SELECT o.* FROM purchase_orders o JOIN users u ON o.user_id = u.id WHERE u.email = ?`

而在 JPQL 中，因为 `PurchaseOrder` 实体里已经通过 `@ManyToOne` 映射了 `User user` 属性，你可以直接用“点”来进行**对象导航**，JPA 底层会自动帮你生成关联表的 JOIN 语句：
- **JPQL**: `SELECT o FROM PurchaseOrder o WHERE o.user.email = :email`

> [!WARNING] 
> **N+1 查询问题 (The N+1 Problem)**
> 
> 虽然对象导航非常方便，但如果你查询出了一批数据（比如 10 个订单），然后在代码里遍历去调用 `order.getUser().getEmail()`，如果配置了懒加载（Lazy Loading），JPA 会先执行 **1** 条 SQL 查出 10 个订单，然后为了获取每个订单的用户，又循环执行 **N** (10) 条 SQL 分别查对应的用户。这就是著名的 **N+1 问题**（或者叫 1+N 问题），多表关联时如果依赖默认的导航去查，会严重拖垮数据库性能！
> 
> **解决方案：**
> 1. **使用 `JOIN FETCH` (推荐)**：在 JPQL 中显式使用 `JOIN FETCH o.user`。这会告诉 JPA 把关联对象一口气通过一条 JOIN SQL 查出来并装配好，而不是等到用到时再触发单条查询。
>    ```java
>    @Query("SELECT o FROM PurchaseOrder o JOIN FETCH o.user WHERE o.status = :status")
>    List<PurchaseOrder> findOrdersWithUser(@Param("status") OrderStatus status);
>    ```
> 2. **使用 `@EntityGraph`**：如果你使用派生查询不想手写 JPQL，可以在方法上加上 `@EntityGraph(attributePaths = {"user"})`，它能起到和 `JOIN FETCH` 一样的预加载效果。


### **命名参数绑定**
在 JPQL 中，推荐使用命名参数（`:参数名`）配合方法上的 `@Param` 注解来传递变量，这样即使打乱参数顺序也不影响，且代码可读性极高：

```java
@Query("SELECT u FROM User u WHERE u.name LIKE %:keyword% AND SIZE(u.orders) > :minCount")
List<User> findComplexUsers(@Param("keyword") String keyword, @Param("minCount") int minCount);
```

> [!TIP]
>
> 如果命名参数与方法参数同名可以直接省略@Param注解

### **更新与删除**
如果你要用 JPQL 执行 `UPDATE` 或 `DELETE` 语句，除了写 `@Query`，还必须在旁边加上 `@Modifying` 注解，告知 JPA 这是一个会修改数据的操作。同时，外部调用该方法的地方必须开启 `@Transactional` 事务：
```java
@Modifying
@Query("UPDATE User u SET u.name = :newName WHERE u.email = :email")
int updateNameByEmail(@Param("newName") String newName, @Param("email") String email);
```

### **分页查询 **
在 JPQL 中进行分页非常简单，完全不需要手写 `LIMIT` 和 `OFFSET`。只需要在方法参数的最后加上 `Pageable` 对象，并将返回值声明为 `Page<T>`，JPA 就会自动帮你进行分页并附带执行一条 `COUNT` 语句来统计总记录数：
```java
@Query("SELECT o FROM PurchaseOrder o WHERE o.status = :status")
Page<PurchaseOrder> findOrdersByStatusWithPage(@Param("status") OrderStatus status, Pageable pageable);
```
**进阶优化（countQuery）**：当你的 JPQL 涉及非常复杂的连表（如多重 `JOIN`）时，JPA 自动推导的 `COUNT` 语句可能会很慢或者报错。这时你可以使用 `countQuery` 属性手动提供一条极简的用于计数的 SQL/JPQL：
```java
@Query(
    value = "SELECT o FROM PurchaseOrder o LEFT JOIN FETCH o.user WHERE o.amount > :minAmount",
    countQuery = "SELECT COUNT(o) FROM PurchaseOrder o WHERE o.amount > :minAmount"
)
Page<PurchaseOrder> findLargeOrdersWithPage(@Param("minAmount") BigDecimal minAmount, Pageable pageable);
```

**JPA 提供的三种分页返回类型对比：**

| 返回类型 | 是否执行 COUNT 查询 | 包含的分页元数据 | 适用业务场景 |
| :--- | :--- | :--- | :--- |
| `Page<T>` | 是 | 包含完整元数据（如 `totalElements` 总条数, `totalPages` 总页数, `hasNext` 等）。 | 传统的完整分页组件（需要展示“共 X 条”、“尾页”按钮的场景）。 |
| `Slice<T>` | 否 | 仅包含当前页数据和 `hasNext`（是否有下一页）。JPA 会在底层多查出 1 条记录来判断是否有下一页。 | 移动端或网页的“无限滚动 / 加载更多”（无需计算总数，性能极大提升）。 |
| `List<T>` | 否 | 无任何分页元数据，仅返回当前页截取的数据列表。 | 纯粹只需获取前 N 条数据（Top N），或者内部接口调用完全不需要分页上下文的场景。 |

**三种返回类型对应的 JSON 结构示例：**

1. **`Page<T>` 结构示例**（包含全量分页元数据，执行了 COUNT 查询）：
```json
{
  "content": [
    { "id": 1, "amount": 99.99, "status": "PAID" },
    { "id": 2, "amount": 199.00, "status": "PAID" }
  ],
  "pageable": {
    "sort": { "empty": true, "sorted": false, "unsorted": true },
    "offset": 0,
    "pageSize": 10,
    "pageNumber": 0,
    "unpaged": false,
    "paged": true
  },
  "last": false,
  "totalPages": 5,        // <-- Page 独有：总页数
  "totalElements": 50,    // <-- Page 独有：总条数
  "size": 10,
  "number": 0,
  "sort": { "empty": true, "sorted": false, "unsorted": true },
  "first": true,
  "numberOfElements": 2,
  "empty": false
}
```

2. **`Slice<T>` 结构示例**（无总数统计，性能更好）：
```json
{
  "content": [
    { "id": 1, "amount": 99.99, "status": "PAID" },
    { "id": 2, "amount": 199.00, "status": "PAID" }
  ],
  "pageable": {
    "sort": { "empty": true, "sorted": false, "unsorted": true },
    "offset": 0,
    "pageSize": 10,
    "pageNumber": 0,
    "unpaged": false,
    "paged": true
  },
  "last": false,          // <-- Slice 主要依靠这个判断是否有下一页
  "size": 10,
  "number": 0,
  "sort": { "empty": true, "sorted": false, "unsorted": true },
  "first": true,
  "numberOfElements": 2,
  "empty": false
}
```

3. **`List<T>` 结构示例**（最纯粹的数据列表，无任何元数据包裹）：
```json
[
  { "id": 1, "amount": 99.99, "status": "PAID" },
  { "id": 2, "amount": 199.00, "status": "PAID" }
]
```
