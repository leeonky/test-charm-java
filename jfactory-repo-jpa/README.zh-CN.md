# jfactory-repo-jpa

[English](README.md)

`jfactory-repo-jpa` 是 [jfactory](../jfactory/README.zh-CN.md) 的 JPA-backed `DataRepository` 实现。它让 JFactory 创建出来的实体可以通过统一 repository 抽象被持久化、被查询，这在需要触达真实持久层的测试里非常有价值。

这个模块的目的不是替代你应用里的 repository 层，而是给 JFactory 提供一个能和实体模型说同一种语言的持久化目标。

## 添加依赖

```groovy
testImplementation "org.test-charm:jfactory-repo-jpa:<version>"
```

## 快速开始

```java
EntityManager entityManager = entityManagerFactory.createEntityManager();
JFactory jFactory = new JFactory(new JPADataRepository(entityManager));

Order order = jFactory.create(Order.class);
Collection<Order> orders = jFactory.type(Order.class).queryAll();
```

这个很短的例子其实已经覆盖了完整链路：

- JFactory 创建实体数据
- repository 通过 JPA 把它持久化
- 同一个 repository 之后还可以再把这些实体查回来供 JFactory 继续查询

这也是它在集成测试里很有用的原因：准备数据和回查数据可以继续停留在同一套 JFactory 流程里。

## 这个 repository 做什么

`JPADataRepository` 实现了 JFactory 所需的最小 repository 合约：

- `save(object)`：在独立事务中持久化实体
- `queryAll(type)`：通过 Criteria API 读取某种实体类型的全部数据
- `clear()`：清空 `EntityManager` 的持久化上下文

虽然接口很小，但已经足够支撑 JFactory 那条最典型的链路：创建数据 -> 保存 -> 再按类型 / spec 查回。

## 关键行为

### `clear()` 不会删表数据

`clear()` 只会清空 JPA 一级缓存，**不会** truncate table，也不会 delete row。

这是一个非常关键的运行时细节。只要你的 scenario 需要干净数据库，就仍然要靠外部测试基础设施去负责真正的数据清理。

### `@Embeddable` 不会作为顶层实体被持久化

Embeddable 类型不会参与顶层 save/query，这和 JPA 本身的语义一致：它们属于实体图的一部分，而不是独立聚合根。

### 支持忽略某些类型的 save/query

你可以传入一组“只参与对象图，不参与 repository 持久化 / 查询”的类型：

```java
new JPADataRepository(entityManager, List.of(IgnoreSaving.class))
```

当对象图里混有纯内存辅助对象，而你又不希望它们被写入数据库时，这很有用。

## 适合什么场景

这个模块适合：

- JFactory 已经负责测试数据创建
- 测试目标走 JPA 持久化
- 你希望少写一层自定义持久化胶水代码

在很多领域模型比较完整的持久化测试里，它往往是更合适的默认 companion module，因为你可以继续站在实体 / spec 这一层写测试，而不是立刻降到 JDBC 表结构层。

## 仓库中的参考示例

实现层面最直接的参考文件是：

- [`src/test/java/org/testcharm/jfactory/repo/JPADataRepositoryTest.java`](src/test/java/org/testcharm/jfactory/repo/JPADataRepositoryTest.java)

## 相关项目

- [jfactory](../jfactory/README.zh-CN.md)：负责创建并再次查询这些由 repository 持久化的对象。
- [DAL-extension-jfactory](../DAL-extension-jfactory/README.zh-CN.md)：把 repository 中的 JFactory 数据直接暴露给 DAL。
- [DAL-extension-jdbc](../DAL-extension-jdbc/README.zh-CN.md)：当你真正要验证的是表结构形态本身时，再直接降到 JDBC。
