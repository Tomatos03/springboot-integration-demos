# MyBatis-Plus

MyBatis-Plus 是一个 MyBatis 的增强工具，在 MyBatis 的基础上只做增强不做改变，为简化开发、提高效率而生。通过提供强大的 CRUD 操作、条件构造器、分页插件等功能，显著降低开发难度，提升开发效率。

## 快速开始

### 添加依赖

在项目的 `pom.xml` 中添加 MyBatis-Plus 依赖：

```xml
<!-- MyBatis-Plus Spring Boot 3 Starter -->
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
    <version>[latest-version]</version>
</dependency>

<!-- 分页插件、乐观锁插件等增强功能 -->
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-jsqlparser</artifactId>
    <version>[latest-version]</version>
</dependency>
```

### 配置应用属性

在 `application.yml` 中配置 MyBatis-Plus 的基础设置。查看完整配置：[application.yml](./src/main/resources/application.yml)

关键配置项说明：
- `mapper-locations`: Mapper XML 文件位置
- `type-aliases-package`: 实体类包路径
- `map-underscore-to-camel-case`: 开启驼峰命名转换
- `id-type`: 主键生成策略（AUTO-自增，ASSIGN_ID-雪花算法）
- `logic-delete-field`: 逻辑删除字段配置

### 定义实体类

创建数据库实体类，使用 `@TableName` 注解指定表名，使用 `@TableId` 注解指定主键。

查看完整示例：[User.java](./src/main/java/com/demo/entity/User.java)

关键注解说明：
- `@TableName`: 指定数据库表名
- `@TableId`: 指定主键，支持多种生成策略
- `@TableField`: 指定字段属性，支持自动填充、逻辑删除等
- `@TableLogic`: 标记逻辑删除字段

### 定义 Mapper 接口

创建 Mapper 接口，继承 `BaseMapper<T>` 即可自动获得基础 CRUD 方法。

查看完整示例：[UserMapper.java](./src/main/java/com/demo/mapper/UserMapper.java)

### 创建 Service 类

创建 Service 类，继承 `ServiceImpl<Mapper, DO>` 以获取丰富的业务方法。

查看完整示例：[UserService.java](./src/main/java/com/demo/service/impl/UserService.java)

基本方法调用：
```java
// 查询单个
User user = service.getById(id);

// 条件查询
LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
wrapper.eq(User::getUsername, "admin");
List<User> users = service.list(wrapper);

// 新增
User user = new User();
service.save(user);

// 修改
service.updateById(user);

// 删除
service.removeById(id);
```

## 配置分页插件

分页插件允许对查询结果进行分页处理，支持多种数据库。

**在配置类中注册分页插件**

创建或修改配置类，注册 `MybatisPlusInterceptor`。查看完整配置：[MybatisPlusConfig.java](./src/main/java/com/demo/config/MybatisPlusConfig.java#L22-37)

**在 `application.yml` 中配置数据库类型**

```yaml
mybatis-plus:
  global-config:
    db-config:
      # 支持: MYSQL, MARIADB, ORACLE, DB2, H2, HSQL, SQLITE, POSTGRE, SQLSERVER2005, SQLSERVER, DM, GBASE, KINGBASE, PHOENIX, GAUSSDB, CUBRID, GOLDILOCKS, INFORMIX, OCEANBASE, MYSQL_8 等
      id-type: AUTO
```

**使用分页查询**

```java
// 创建分页对象（当前页码, 每页大小）
Page<User> page = new Page<>(1, 10);

// 添加查询条件（可选）
LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
wrapper.eq(User::getStatus, 1);

// 执行分页查询
IPage<User> result = userMapper.selectPage(page, wrapper);

// 获取结果
List<User> records = result.getRecords();      // 当前页数据
long total = result.getTotal();                 // 总记录数
long pages = result.getPages();                 // 总页数
```

## 公共字段自动填充

自动填充功能可以在新增或更新数据时，自动设置某些字段的值（如创建时间、更新时间）。

**在实体类中标记需要填充的字段**

查看字段定义：[User.java#L47-55](./src/main/java/com/demo/entity/User.java#L47-55)

使用注解标记：
- `@TableField(fill = FieldFill.INSERT)`: 新增时填充
- `@TableField(fill = FieldFill.UPDATE)`: 修改时填充
- `@TableField(fill = FieldFill.INSERT_UPDATE)`: 新增和修改时都填充

**实现 `MetaObjectHandler` 接口**

创建配置类实现自动填充逻辑。查看完整实现：[MetaObjectHandlerConfig.java](./src/main/java/com/demo/config/MetaObjectHandlerConfig.java)

```java
@Component
public class MetaObjectHandlerConfig implements MetaObjectHandler {
    
    @Override
    public void insertFill(MetaObject metaObject) {
        // 新增时填充
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
    }
    
    @Override
    public void updateFill(MetaObject metaObject) {
        // 修改时填充
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
}
```

**正常执行增删改操作，自动填充会生效**

```java
User user = new User();
user.setUsername("张三");
// 不需要手动设置 createTime，会自动填充为当前时间
userService.save(user);
```

## 枚举类型配置

MyBatis-Plus 提供了完整的枚举支持，包括存储、加载、序列化等方面的配置。

**创建枚举类并添加必要注解**

查看完整枚举定义：[UserStatus.java](./src/main/java/com/demo/enums/UserStatus.java)

枚举配置说明：
- `@EnumValue`: 标记数据库存储值的字段（如整数编码）
- `@JsonValue`: 标记 JSON 序列化时使用的值（如描述字符串）
- `@JsonCreator`: 标记 JSON 序列化的方法，支持灵活的转换逻辑

**在实体类中使用枚举字段**

在 User 实体中使用 `UserStatus` 枚举：[User.java#L41-50](./src/main/java/com/demo/entity/User.java#L41-50)

```java
@TableField("status")
private UserStatus status;
```

**配置自动转换**

MyBatis-Plus 会自动处理以下转换：

| 转换步骤 | 说明 |
|---------|------|
| **入参转换** | 前端发送POST请求，请求体 Person { status: "active",   name: "xiaoming" }，通过 `@JsonCreator` 将status的值转换为 `UserStatus` 枚举 |
| **存储转换** | MyBatis-Plus 使用 `@EnumValue` 的值存储到数据库（如 1, 2, 3） |
| **加载转换** | MyBatis-Plus 从数据库读取值（1, 2, 3），自动转换为对应的 `UserStatus` 枚举 |
| **响应序列化** | 使用 `@JsonValue` 将枚举序列化为 JSON（如 "active", "inactive", "block"） |

## 条件构造器

MyBatis-Plus 提供了强大的条件构造器，支持链式调用，使条件查询更加优雅。

### LambdaQueryWrapper 常用方法

```java
LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

// 比较操作
wrapper.eq(User::getId, 1)                     // 等于 =
       .ne(User::getUsername, "admin")         // 不等于 !=
       .gt(User::getAge, 18)                   // 大于 >
       .ge(User::getAge, 18)                   // 大于等于 >=
       .lt(User::getAge, 60)                   // 小于 <
       .le(User::getAge, 60);                  // 小于等于 <=

// 模糊查询
wrapper.like(User::getUsername, "张")          // 模糊查询 LIKE %张%
       .likeLeft(User::getUsername, "三")      // 左模糊查询 LIKE %三
       .likeRight(User::getUsername, "张");    // 右模糊查询 LIKE 张%

// 范围查询
wrapper.between(User::getAge, 18, 30)          // 区间查询 BETWEEN ... AND ...
       .in(User::getAge, Arrays.asList(18, 20, 22));  // IN 查询

// 空值判断
wrapper.isNull(User::getEmail)                 // 为空 IS NULL
       .isNotNull(User::getEmail);             // 不为空 IS NOT NULL

// 排序
wrapper.orderByAsc(User::getAge)               // 升序
       .orderByDesc(User::getCreateTime);      // 降序

// 执行查询
List<User> users = userService.list(wrapper);
```

### QueryWrapper 用法

`QueryWrapper` 使用字符串字段名，相对 `LambdaQueryWrapper` 不够类型安全，但在某些场景仍然适用：

```java
QueryWrapper<User> wrapper = new QueryWrapper<>();
wrapper.eq("username", "admin")
       .like("email", "@qq.com")
       .orderByDesc("create_time");
```

### 使用 Wrappers 工具类

MyBatis-Plus 提供了 `Wrappers` 工具类，用于快速构建各种类型的条件构造器，代码更简洁：

```java
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

// 构建 LambdaQueryWrapper (推荐)
List<User> users1 = userService.list(
    Wrappers.lambdaQuery(User.class)
        .eq(User::getUsername, "admin")
        .like(User::getEmail, "@qq.com")
        .orderByDesc(User::getCreateTime)
);

// 构建 QueryWrapper
List<User> users2 = userService.list(
    Wrappers.query(User.class)
        .eq("username", "admin")
        .like("email", "@qq.com")
        .orderByDesc("create_time")
);

// 构建更新条件 - LambdaUpdateWrapper
userService.update(
    null,
    Wrappers.lambdaUpdate(User.class)
        .set(User::getAge, 25)
        .eq(User::getUsername, "admin")
);

// 构建更新条件 - UpdateWrapper
userService.update(
    null,
    Wrappers.update(User.class)
        .set("age", 25)
        .eq("username", "admin")
);

// 分页查询结合 Wrappers
Page<User> page = new Page<>(1, 10);
IPage<User> result = userService.page(
    page,
    Wrappers.lambdaQuery(User.class)
        .ge(User::getAge, 18)
        .le(User::getAge, 60)
        .orderByDesc(User::getCreateTime)
);
```

**Wrappers 常用方法说明：**

| 方法 | 说明 |
|------|------|
| `Wrappers.lambdaQuery(User.class)` | 构建 LambdaQueryWrapper（推荐） |
| `Wrappers.query(User.class)` | 构建 QueryWrapper |
| `Wrappers.lambdaUpdate(User.class)` | 构建 LambdaUpdateWrapper |
| `Wrappers.update(User.class)` | 构建 UpdateWrapper |

使用 `Wrappers` 的优势：
- 链式调用更流畅，代码更简洁
- 无需提前创建 Wrapper 对象
- 支持泛型，类型安全
- 适合在业务代码中快速构建查询条件
