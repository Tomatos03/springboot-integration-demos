# MyBatis-Plus

MyBatis-Plus 是一个 MyBatis 的增强工具，在 MyBatis 的基础上只做增强不做改变，为简化开发、提高效率而生。

## 依赖导入

```xml
<!-- MyBatis-Plus Spring Boot 3 Starter -->
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
    <version>3.5.14</version>
</dependency>

<!-- 需要分页插件, 乐观锁插件等额外的增强功能时需要导入 -->
<!-- MyBatis-Plus JSQLParser(提供分页插件, 乐观锁插件等多种功能) -->
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-jsqlparser</artifactId>
    <version>3.5.14</version>
</dependency>
```

## 基本使用

### 1. 配置实体类

```java
@Data
@TableName("user") // 数据库之中对应的表名
public class User {
    // 指定表的主键
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    // 显示指定表字段, 如果没有显示指定默认为字段名默认为变量名
    @TableField("username")
    private String username;
    // 开启自动填充
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableLogic
    private Integer deleted;
}
```

### 2. 定义Mapper接口

```java
// 继承Mybatis-Plus提供的BaseMapper<DO>接口, 继承基本的CURD方法定义
public interface UserMapper extends BaseMapper<User> {
    // 继承BaseMapper后自动拥有增删改查方法
    // 可以添加自定义方法
    List<User> selectByUsernameLike(@Param("username") String username);
}
```

### 3. 创建Service类

```java
// 继承Mybatis-plus提供的ServiceImpl<Mapper, DO>类获取实例方法 
@Service
public class UserService extends ServiceImpl<UserMapper, User> implements IUserService {
    // 继承ServiceImpl后自动拥有常用业务方法
    
    public List<User> getUsersByCondition(String username, Integer minAge, Integer maxAge) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(username != null, User::getUsername, username)
                    .ge(minAge != null, User::getAge, minAge)
                    .le(maxAge != null, User::getAge, maxAge);
        return list(queryWrapper);
    }
}
```

### 4. 自定义SQL XML（可选）

对于复杂的SQL, Mybatis-Plus提供的API可能无法直接满足需求, 这个时候需要手写SQL, 配置手写SQL的方式和Mybatis一样

```xml
<select id="selectByUsernameLike" resultMap="BaseResultMap">
    SELECT * FROM user
    WHERE deleted = 0
    <if test="username != null and username != ''">
        AND username LIKE CONCAT('%', #{username}, '%')
    </if>
    ORDER BY create_time DESC
</select>
```

## 配置说明

### 1. application.yml 核心配置

```yaml
# MyBatis-Plus配置
mybatis-plus:
  # Mapper XML文件位置
  mapper-locations: classpath:mapper/*.xml
  # 类型别名包
  type-aliases-package: com.demo.entity
  configuration:
    # 开启驼峰命名转换
    map-underscore-to-camel-case: true
    # 日志实现
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      # 主键类型（AUTO-数据库自增）
      id-type: AUTO
      # 逻辑删除配置
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
```

### 2. 分页插件配置

在 `MybatisConfig.java` 中配置：

```java
@Bean // 声明为 Spring Bean，交由 Spring 容器管理
public MybatisPlusInterceptor mybatisPlusInterceptor() {
    // 创建 MyBatis-Plus 拦截器实例
    MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

    // 创建分页插件实例
    PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor();

    // 设置数据库类型为 H2（如使用 MySQL 可改为 DbType.MYSQL）
    paginationInnerInterceptor.setDbType(DbType.H2);

    // 将分页插件添加到拦截器中
    interceptor.addInnerInterceptor(paginationInnerInterceptor);

    // 返回配置好的拦截器
    return interceptor;
}
```

注册好对应的`Bean对象`之后, 参考如下代码进行分页查询:

```java
public IPage<User> selectUserPage(int pageNum, int pageSize, String username) {
    // 创建内置的Page并提供pageNum(页码), pageSize(单页大小)
    Page<User> page = new Page<>(pageNum, pageSize);
    // 如果有额外的条件可以使用条件构造器
    QueryWrapper<User> wrapper = new QueryWrapper<>();
    wrapper.like("username", username);
    // 使用BaseMapper提供的内置分页查询方法
    return userMapper.selectPage(page, wrapper);
}
```

### 3. 自动填充配置

```java
// 注册MetaObjectHandler实现类为Bean对象
@Component
public class MetaObjectHandlerConfig implements MetaObjectHandler {
    
    /**
     * 插入时自动填充
     * 当执行 insert 操作时，会自动填充标注了 @TableField(fill = FieldFill.INSERT) 的字段
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        // 自动填充创建时间
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        // 自动填充更新时间
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
    
    /**
     * 更新时自动填充
     * 当执行 update 操作时，会自动填充标注了 @TableField(fill = FieldFill.UPDATE) 的字段
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        // 自动填充更新时间
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
}

```

## MyBatis-Plus API参考

### BaseMapper API

| 方法 | 说明 |
|------|------|
| insert(T entity) | 插入一条记录 |
| deleteById(Serializable id) | 根据ID删除 |
| updateById(T entity) | 根据ID更新 |
| selectById(Serializable id) | 根据ID查询 |
| selectList(Wrapper<T> queryWrapper) | 条件查询列表 |
| selectPage(Page<T> page, Wrapper<T> queryWrapper) | 分页查询 |

### IService API

| 方法 | 说明 |
|------|------|
| save(T entity) | 保存记录 |
| saveBatch(Collection<T> entityList) | 批量保存 |
| removeById(Serializable id) | 根据ID删除 |
| updateById(T entity) | 根据ID更新 |
| getById(Serializable id) | 根据ID查询 |
| list(Wrapper<T> queryWrapper) | 条件查询列表 |
| page(Page<T> page, Wrapper<T> queryWrapper) | 分页查询 |
| count(Wrapper<T> queryWrapper) | 统计数量 |

## 条件构造器参考

### LambdaQueryWrapper API

```java
LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
queryWrapper
    .eq(User::getId, 1)                          // 等于
    .ne(User::getUsername, "admin")              // 不等于
    .gt(User::getAge, 18)                        // 大于
    .ge(User::getAge, 18)                        // 大于等于
    .lt(User::getAge, 60)                        // 小于
    .le(User::getAge, 60)                        // 小于等于
    .like(User::getUsername, "张")               // 模糊查询
    .likeLeft(User::getUsername, "三")           // 左模糊查询 %三
    .likeRight(User::getUsername, "张")          // 右模糊查询 张%
    .between(User::getAge, 18, 30)               // 区间查询
    .in(User::getAge, Arrays.asList(18, 20, 22)) // IN查询
    .isNull(User::getEmail)                      // 为空
    .isNotNull(User::getEmail)                   // 不为空
    .orderByAsc(User::getAge)                    // 升序
    .orderByDesc(User::getCreateTime);           // 降序
```


## 参考资料

- [MyBatis-Plus官方文档](https://baomidou.com/)
- [MyBatis-Plus GitHub](https://github.com/baomidou/mybatis-plus)