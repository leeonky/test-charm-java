# DAL-extension-jdbc

[English](README.md)

`DAL-extension-jdbc` 会把关系型数据库表转换成 DAL 数据：连上数据库后，表会变成属性，行会变成对象，关系也可以像普通数据导航一样继续往下走。

但这个模块更适合被理解为一个**面向表结构的集成工具**，而不是一上来就默认使用的持久层方案。

## 添加依赖

```groovy
testImplementation "org.test-charm:DAL-extension-jdbc:<version>"
```

## 如果已经有领域模型，优先从领域模型出发

对大多数应用层测试来说，更推荐的路径通常是：

1. 先把数据关系建成合规、清晰的领域对象模型
2. 通过 JPA 或你的 repository 层去持久化
3. 用 [jfactory-repo-jpa](../jfactory-repo-jpa/README.zh-CN.md) 和 [DAL-extension-jfactory](../DAL-extension-jfactory/README.zh-CN.md) 做准备和查询
4. 再通过 Java 对象或 spec 驱动的 DAL 数据去断言

这样测试看到的是和应用本身一致的对象关系。

`DAL-extension-jdbc` 更适合这些情况：

- 你在验证一个并不适合直接映射成领域对象的旧库表结构
- 你要检查 join table、报表表、去范式表等“关系形状本身”
- 你要验证数据库迁移或 ETL 结果
- 你所在项目里，JDBC 就是最可靠的集成边界

换句话说：如果项目已经有干净的领域模型和 repository 抽象，应该优先走那个更高层的路径；只有当测试本身就是在验证“表形态”时，再直接降到 `DAL-extension-jdbc`。

## 快速开始

```java
Connection connection = DriverManager.getConnection("jdbc:h2:mem:test", "sa", "");
DataBaseBuilder builder = new DataBaseBuilder();

expect(builder.connect(connection)).should("""
  : {
    products: | id | name   |
              | p1 | iPhone |
              | p2 | MBP    |
  }
""");
```

这个例子已经把模块的核心思想表现出来了：

- 数据库连接被包装成 DAL 根对象
- `products` 这样的表名变成 DAL 属性
- 每一行都可以继续按对象、表格和关系路径来断言

这样数据库校验就不再需要写很多临时 SQL + imperative assertion code。

## 数据模型

在 `connect(...)` 之后：

- 整个数据库是 DAL 根对象
- 每张表会以属性形式暴露出来，例如 `products`、`orders`
- 每一行都像一个带列属性的对象

所以你可以自然地写出：

```dal
products::size = 2

products: [{
  id= 10L
  name= MBP
}]
```

第一段是在校验数量，第二段是在校验行内容。两者都继续停留在 DAL 的同一种表达风格里。

## 过滤与投影

最常用的两个子句是：

- `::where[...]`
- `::select[...]`

例如：

```dal
: {
  products::where[name='MBP']: [{ name= MBP }]
  products::select[name as n].n[]: [MBP iPod]
}
```

它们的价值在于：

- `::where[...]` 可以把简单过滤直接贴在断言旁边，而不是额外写一段 SQL
- `::select[...]` 可以只挑出你真正想比较的列或派生值

这也是这个模块非常顺手的一点：很多数据库校验可以保持很短，但不会因为太短而失去可读性。

## 关系导航

扩展支持常见的关系遍历：

- `::belongsTo[...]`
- `::hasOne[...]`
- `::hasMany[...]`
- 多对多关系

例如：

```dal
orders: [{
  customer= Tom
  ::hasMany[order_lines]: | id | quantity | ::belongsTo[products].name |
                          | 1  | 1        | iPhone                     |
                          | 2  | 100      | MBP                        |
}]
```

这段断言本质上是在按业务语义阅读数据库：

- 先从某一行订单开始
- 向下走到它的订单明细
- 再从每个明细走到商品
- 最终把整个关联结果当成一个嵌套数据结构去验证

所以更好的理解方式不是“把 SQL 塞进 DAL”，而是“用 DAL 的方式表达表关系导航”。

## Row method

如果你不希望断言里直接出现技术味很重的表名，可以给行策略注册更贴近业务的名称：

```java
DataBaseBuilder builder = new DataBaseBuilder();

builder.tableStrategy("orders").registerRowMethod("orderLines",
    row -> row.hasMany("order_lines"));

builder.tableStrategy("order_lines").registerRowMethod("product",
    row -> row.belongsTo("products"));
```

之后断言就可以写成：

```dal
orders: [{
  orderLines: | id | quantity | product.name |
              | 1  | 1        | iPhone       |
              | 2  | 100      | MBP          |
}]
```

当原始表名偏技术化、而测试希望保持业务可读性时，这通常很值得做。

## 适合什么场景

`DAL-extension-jdbc` 适合那些希望数据库断言保持下面这些特征的场景：

- 结构化，而不是字符串拼接式
- 紧贴真实持久化形态
- 不用写大量 SQL 和 ResultSet 遍历代码

但如果你的系统已经有成熟的领域模型、JPA repository 和 JFactory spec，那么应该默认保持那个更高层的测试视角，只在“关系表形态本身就是测试对象”时才下沉到 JDBC。

## 仓库中的参考示例

仓库里比较有代表性的示例在这些文件中：

- [`src/test/resources/features/single-table.feature`](src/test/resources/features/single-table.feature)
- [`src/test/resources/features/belongs-to.feature`](src/test/resources/features/belongs-to.feature)
- [`src/test/resources/features/has-one.feature`](src/test/resources/features/has-one.feature)
- [`src/test/resources/features/has-many.feature`](src/test/resources/features/has-many.feature)
- [`src/test/resources/features/many-to-many.feature`](src/test/resources/features/many-to-many.feature)

## 相关项目

- [DAL-java](../DAL-java/README.zh-CN.md)：核心语法和结构化断言能力。
- [DAL-extension-jfactory](../DAL-extension-jfactory/README.zh-CN.md)：把 JFactory 的 repository 数据直接暴露给 DAL。
- [jfactory-repo-jpa](../jfactory-repo-jpa/README.zh-CN.md)：在很多领域建模良好的项目里，这是更适合作为默认路径的持久化方案。
