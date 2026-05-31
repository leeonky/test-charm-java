# jfactory-cucumber

[English](README.md)

`jfactory-cucumber` 是 [jfactory](../jfactory/README.zh-CN.md) 在 Cucumber 里的胶水层。大多数团队使用这个模块，并不是因为想多一层 Java API，而是因为他们想**直接复用现成 Step**。这是理解这个库最合适的方式。

它的职责，是让 feature 文件用业务数据的语言来描述测试数据，而实际的数据创建、关联、持久化、后续查询仍然交给 JFactory 处理。

核心类型是 `JData`，内建 Step 本质上都是围绕它的一层可复用封装。

## 添加依赖

```groovy
testImplementation "org.test-charm:jfactory-cucumber:<version>"
```

## 使用前提

这一部分非常重要，因为很多人第一次用不好，不是 Step 不会写，而是前提没准备好。

### 1. Spec 本身要先组织好

这个模块不会替你发明数据模型，它默认你已经有：

- JFactory Spec 类
- 清晰可读的 Spec 名称
- 用 Trait 表达业务变体
- 适合你测试风格的 repository 策略

如果这些底层组织得很乱，feature 文件再怎么写也不会真正简洁。

### 2. `JFactory` 必须被注入到 Cucumber 运行时

`JData` 是从 `JFactory` 构造出来的：

```java
JData jData = new JData(jFactory);
```

所以在真实项目里，Step 运行时必须先通过 Cucumber 的依赖注入机制拿到一个共享的 `JFactory`。

一个典型的 Spring Boot 例子如下：

```java
@Configuration
public class FactoryConfiguration {
    @PersistenceUnit
    private EntityManagerFactory entityManagerFactory;

    @Bean
    public JFactory createJFactory() {
        return new JFactory(new JPADataRepository(entityManagerFactory.createEntityManager()))
            .register(Products.Product.class)
            .register(Orders.Order.class);
    }
}
```

这个例子很关键，因为它说明了两件事：

- Step 层依赖的是**一个已经配置好的 JFactory**
- Spec 注册通常就在这里完成

### 3. Repository 清理仍然要由你负责

如果你的 repository 背后是真实数据库或其他持久化存储，那么每个 scenario 之间的数据清理仍然要靠你自己的测试基础设施去做。`jfactory-cucumber` 让数据准备更简洁，但不会替代环境隔离策略。

## 怎么理解这些 Step 里的小表达式

这个模块里反复出现三种短表达式：

- `traitsSpec`：Trait + Spec 表达式，例如 `WrongPassword LoginRequest`
- `queryExpression`：查询表达式，例如 `Order.code[S01]`
- `beanProperty`：查询表达式再加属性路径，例如 `Order.code[S01].items`

正是这些短表达式，让 Step 在保持简洁的同时还能操作复杂对象图。

## 内建 Step 家族

### 1. 用显式值准备数据

```gherkin
Given Exists data "Product":
  | name | color |
  | book | red   |
```

这个 Step 背后做的事是：

- 选择 `Product` 这个 spec
- 每一行创建一个对象
- 每一列映射成 JFactory property
- 最终通过 JFactory 的 repository 保存下来

对新手来说，这通常是最容易上手的一种写法，因为它最接近业务数据表。

### 2. 在一张表里准备层级数据

```gherkin
Given Exists data "Cart":
  | customer | products[0](Product).name | products[0].stocks[0](Inventory).size | products[0].stocks[0].count |
  | Tom      | book                      | A4                                    | 100                         |
```

这个例子值得仔细看，因为它体现了模块最重要的表达风格：

- 根对象是 `Cart`
- 子对象在同一行里直接建出来
- `(Product)`、`(Inventory)` 这类 spec 标记让嵌套创建保持显式
- 一行表格就能表达一小棵对象树

这正是这个模块最大的生产力来源：feature 文件仍然是声明式的，但数据形状已经不再局限于扁平结构。

### 3. 用默认值创建数据

```gherkin
Given Exists 1 data "Red Product"
```

这个 Step 的意思是：按 `Red Product` 这个 trait/spec 组合，直接创建一个默认对象。

当 Spec 已经把默认数据形态描述得很好，而当前场景又不需要重复写明细字段时，这种写法最合适。

### 4. 用 DAL 风格文档准备数据

```gherkin
Given Exists data "Product":
  """
  name: 'book'.toUpperCase
  color: red
  """
```

这种形式很重要，因为它让 feature 文件继续停留在 Test Charm 一贯的数据语言风格里。特别适合：

- 表格写起来别扭
- 值本身是嵌套结构
- 用 DAL 表达计算值更自然

### 5. 给已有对象挂子数据

```gherkin
And exists "Cart.customer[Tom].products" as data "Product":
  | name    |
  | bicycle |
```

它表达的意思是：先找到已有 `Cart`，走到它的 `products` 属性，然后把新建的 `Product` 数据挂进去。

这个 Step 很实用，因为验收测试里经常不是“一次把整棵图都建完”，而是先建立主对象，再在后续步骤里继续往图上挂数据。

### 6. 用反向关联创建相关数据

```gherkin
And exists following data "Inventory", and its "product" is "Product.name[book]":
  | size | count |
  | A3   | 10    |
```

这是反向的写法：

- 先找到父对象
- 再创建子对象，并把它的反向关联属性指回父对象

当代码里的自然拥有方向和 feature 文件里最易读的描述方向相反时，这种写法尤其合适。

### 7. 验证单个对象

```gherkin
Then Data "Product.name[book]" should be:
  """
  .name='book'
  and .color='red'
  """
```

这一步会先查询出一个对象，再用 DAL 去验证它。

### 8. 验证集合

```gherkin
Then All data "Product" should be:
  """
  .size=2
  and [0].name='book'
  and [1].name='bicycle'
  """
```

这一步会查询所有匹配数据，并把结果按集合来验证。

### 9. 从 JFactory 整体视角验证全部数据

```gherkin
Then should be:
  """
  : {
    Product: [{ name: book }]
  }
  """
```

当一个 scenario 想从 JFactory repository 的整体视角一次性断言多个 spec 分组时，这种写法很方便。

## 一个更完整的端到端示例

```gherkin
Scenario: create and verify cart data
  Given Exists data "Cart":
    | customer | products[0](Product).name |
    | Tom      | book                      |
  And Exists "Cart.customer[Tom].products" as data "Product":
    | name    |
    | bicycle |
  Then Data "Cart.customer[Tom]" should be:
    """
    .customer='Tom'
    and .products.size=2
    and .products[0].name='book'
    and .products[1].name='bicycle'
    """
```

这个例子比较完整，因为它同时覆盖了：

- 创建根数据
- 后续继续扩展对象图
- 用业务化表达式查询数据
- 最终通过 DAL 验证结果

这就是这个模块最常见、也最值得复用的使用流程。

## 英文 Step 和中文 Step

模块同时内置了英文和中文 Step。团队通常会按自己的 feature 风格选定一种语言并保持统一；这个库支持双语，是为了让 Step 词汇本身和团队的写作用语保持一致。

## 仓库中的参考示例

仓库中比较好的入口包括：

- [`src/test/resources/features/en/prepare-table.feature`](src/test/resources/features/en/prepare-table.feature)
- [`src/test/resources/features/en/assert.feature`](src/test/resources/features/en/assert.feature)
- [`src/test/resources/features/cn/prepare-dal.feature`](src/test/resources/features/cn/prepare-dal.feature)
- [`src/test/resources/features/cn/prepare-table.feature`](src/test/resources/features/cn/prepare-table.feature)

## 相关项目

- [jfactory](../jfactory/README.zh-CN.md)：底层数据创建引擎和 Spec 体系。
- [DAL-java](../DAL-java/README.zh-CN.md)：验证步骤里使用的数据断言语言。
- [RESTful-cucumber](../RESTful-cucumber/README.zh-CN.md)：当场景既要发 API，又要配合 JFactory 数据准备时，常常一起使用。
