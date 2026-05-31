# jfactory

[English](README.md)

`jfactory` 和 [DAL-java](../DAL-java/README.zh-CN.md) 一起，是 Test Charm 的两块核心库之一。它是一个围绕 Spec、Trait、repository 查询复用、以及紧凑数据描述来组织的数据创建库。

它的核心不是“让你更快地写 constructor”，而是：

- 先把有意义的数据形态定义好
- 当前场景只覆盖真正关心的部分
- 该复用已有 repository 数据时优先复用
- 让数据创建和数据验证都保持数据优先的风格

这也是它和很多类似工厂 / fixture 库最大的不同：它不只是对象生成器，而是连接**数据设计**、**数据存储**、**数据复用**的一层。

## 添加依赖

```groovy
testImplementation "org.test-charm:jfactory:<version>"
```

## 先建立一个整体心智

先记住四个概念，后面的 API 会自然很多：

1. **Type builder**：`jFactory.type(Order.class)` 直接从 Java 类型创建数据。
2. **Spec**：`Spec<T>` 给一类数据形态命名，并且复用它。
3. **Trait**：在 Spec 之上叠加具名变体。
4. **Repository**：创建出来的数据会被保存，也可以被查询，因此后续创建的数据可以复用前面已经存在的数据，而不是盲目重复创建。

这四个点一旦串起来，JFactory 的大部分设计都会显得很自然。

## 快速开始

```java
JFactory jFactory = new JFactory();

Order order = jFactory.create(Order.class);
Order customized = jFactory.type(Order.class)
    .property("customer.name", "Alice")
    .property("total", 100)
    .create();
```

这个例子有三个关键信息：

- `create(Order.class)` 会先给你一个带默认值的可用对象
- `.property(...)` 只覆盖当前场景关心的字段
- `customer.name` 这类嵌套路径让准备数据保持很紧凑

所以这里其实已经体现出 JFactory 的风格：不要手工从零拼装全部对象，也不要每次都重复写默认值。

## `type(...)`：直接从类型创建

当你就是想围绕某个 Java 类型直接描述数据时，用 `type(...)`：

```java
jFactory.type(Order.class)
    .property("customer.name", "Alice")
    .property("items[0].name", "Pencil")
    .property("items[0].quantity", 2)
    .create();
```

它适合一次性的、就地的数据准备。

需要注意的不是只有语法本身，而是背后的意义：

- 嵌套对象会自动帮你创建出来
- 集合可以按结构化路径去描述
- 测试代码聚焦的是业务数据，而不是 constructor 和 setter 的装配顺序

## `Spec<T>`：可复用的数据设计

当一类数据形态值得被命名和复用时，就应该定义 `Spec<T>`：

```java
public class PaidOrder extends Spec<Order> {
    @Override
    public void main() {
        property("status").value("PAID");
        property("total").value(100);
    }
}
```

创建方式：

```java
Order order = jFactory.spec(PaidOrder.class).create();
```

Spec 是 JFactory 可读性的核心。它让测试代码可以直接说“创建一个已支付订单”，而不是每次都重新描述“已支付订单长什么样”。

## Trait：具名变体

Trait 是在 Spec 基础上的命名调整：

```java
public class UserSpec extends Spec<User> {
    @Override
    public void main() {
        property("enabled").value(true);
    }

    @Trait
    public UserSpec admin() {
        property("role").value("ADMIN");
        return this;
    }
}
```

使用方式：

```java
User user = jFactory.spec(UserSpec.class).traits("admin").create();
```

Trait 很重要，因为它让测试说的是“我需要哪种数据”，而不是“我需要把哪些字段改成什么值”。

## 用 DAL 风格输入创建数据

在 Test Charm 体系里，JFactory 很常见的一种实际用法不是纯 Java builder，而是直接吃 DAL 风格的数据描述：

```java
jFactory.useDAL().create("PaidOrder", """
  {
    customer: {
      name: Alice
    }
    items: [{
      name: Pencil
      quantity: 2
    }]
  }
""");
```

这很关键，因为它让“创建数据”和“验证数据”都落在同一类语言风格里：

- JFactory 负责按紧凑数据描述创建
- DAL 负责按紧凑数据描述验证

这正是整套 Test Charm 方法论里非常核心的一点。

## DataRepository：不只是“把创建结果存一下”

这是 JFactory 非常重要、也非常区别于很多类似库的一块能力。

`JFactory` 总是和一个 `DataRepository` 一起工作：

- 默认构造器会使用内存 repository
- 你也可以传入自定义 repository

```java
JFactory jFactory = new JFactory(repository);
```

### `save(object)`

每次 JFactory 创建出对象之后，repository 都可以通过 `save(object)` 把它保存到合适的地方。

这个“合适的地方”完全取决于 repository 实现，可以是：

- 数据库
- 文件存储
- 缓存
- mock server 维护的数据区
- 一个内存列表

所以 JFactory 从来不只是“在当前 JVM 里 new 几个对象”，它也可以直接把测试数据创建到整个测试真正会访问的位置上。

### `queryAll(type)`

`queryAll(type)` 的职责是：把某一类型的全部候选数据从 repository 中加载出来。之后，JFactory 会在**内存中**继续做属性条件过滤。

这种设计是有意的，它让 repository 接口保持很小、很好实现：

- repository 只要负责“按类型把所有数据取出来”
- 更丰富的属性匹配、嵌套条件匹配由 JFactory 保留在自己内部完成

因此 repository 实现可以保持轻量，但 JFactory 仍然可以提供很强的查询表达力。

### `query()`

当你调用 `query()` 时，JFactory 的语义是“我要一个明确的唯一结果”：

- 正好一个结果 -> 返回它
- 没有结果 -> 返回 `null`
- 多个结果 -> 立即失败

这种严格性非常重要，因为它避免了“本来想复用已有数据，结果模糊地命中了多个对象”的歧义。

## 嵌套属性创建时，会优先复用已有的关联数据

这是 JFactory 非常有代表性的一项能力。

一个很常见的场景是：

1. 你正在创建一个 `Order`
2. 你已经知道 `Order.customer` 的识别属性，比如 `email`
3. JFactory 应该先去 repository 里找匹配的 `Customer`
4. 只有找不到时，才创建新的 `Customer`

JFactory 对这种模式有直接支持，而且普通的嵌套属性 API 就能表达出来。

### 场景 1：还没有匹配的关联对象

```java
public class Customer {
    public String email;
}

public class Order {
    public Customer customer;
}

JFactory jFactory = new JFactory();

Order order = jFactory.type(Order.class)
    .property("customer.email", "new@example.com")
    .create();
```

这里背后的行为是：

- JFactory 发现要给 `customer.email` 赋值，`Order.customer` 这个对象必须先存在
- 它会先去 repository 里找匹配的 `Customer`
- 这时还没有匹配对象
- 所以它会新建一个 `Customer`
- 最终 `order.customer.email` 就是你输入的 `new@example.com`

也就是说，你虽然只写了一条嵌套属性，但 JFactory 会把需要的对象图补出来。

### 场景 2：已经有匹配的关联对象

```java
JFactory jFactory = new JFactory();

Customer existing = jFactory.type(Customer.class)
    .property("email", "exist@example.com")
    .create();

Order order = jFactory.type(Order.class)
    .property("customer.email", "exist@example.com")
    .create();
```

这个例子说明的是：

- 最终得到的 `order.customer.email` 仍然就是你指定的那个值
- 但 JFactory 会先尝试用 repository 里的现有数据满足这条关联
- 因为已经有匹配的 `Customer`，所以不会再额外创建一个新的

这类行为的价值在于，一条很短的属性路径就表达出了很实际的规则：

- “我要一个订单”
- “它的 customer 必须有这个识别值”
- “如果 repository 里已有匹配 customer，就直接复用；没有再创建”

这项能力为什么重要：

- 可以保证场景中的关联数据保持一致
- 可以避免重复创建本来应该共享的对象
- 可以让对象图自然接到前面已经创建过的 repository 数据上
- 让 JFactory 更适合做集成测试中的数据准备，而不只是 isolated object generation

这个“先查找，查不到再创建”的特性，是 JFactory 很重要、也很有辨识度的能力。

## 实际上怎么组织 Spec

一个很实用的组织方式通常是：

- 每个有业务意义的数据形态对应一个 Spec 类
- 用 Trait 表达具名业务变体
- 当某个关联应该优先接到已有数据时，直接使用 `customer.email` 这类能表达识别属性的嵌套路径
- 当 Java builder 写起来太啰嗦时，直接切到 DAL 风格输入

这样测试规模变大后，可读性仍然比较稳定。

## 仓库中的参考示例

如果你想继续看仓库里的实际示例，下面这些文件尤其值得看：

- [`src/test/resources/features/4-data-repository/1-save-to-repo.feature`](src/test/resources/features/4-data-repository/1-save-to-repo.feature)
- [`src/test/resources/features/4-data-repository/2-query-from-repo.feature`](src/test/resources/features/4-data-repository/2-query-from-repo.feature)
- [`src/test/resources/features/legacy/bug/builder-value-producer.feature`](src/test/resources/features/legacy/bug/builder-value-producer.feature)
- [`src/test/resources/features/input-property/nested-object.feature`](src/test/resources/features/input-property/nested-object.feature)

## 相关项目

- [DAL-java](../DAL-java/README.zh-CN.md)：与 JFactory 深度配合的数据语言，用于创建输入和断言。
- [jfactory-cucumber](../jfactory-cucumber/README.zh-CN.md)：围绕 JFactory 的标准 Cucumber 胶水层。
- [jfactory-repo-jpa](../jfactory-repo-jpa/README.zh-CN.md)：JPA-backed repository 实现。
- [DAL-extension-jfactory](../DAL-extension-jfactory/README.zh-CN.md)：把 repository 中的 JFactory 数据暴露给 DAL。
