# bean-util

[English](README.md)

`bean-util` 是 [DAL-java](../DAL-java/README.zh-CN.md) 和 [jfactory](../jfactory/README.zh-CN.md) 共用的底层反射与属性访问库。大多数人不会把它当成首先接触的模块，但它解释了 Test Charm 很关键的一点：这套框架为什么能用比很多传统 bean 工具更少的样板代码去读取和填充数据。

这个文档会刻意保持简洁。`bean-util` 是基础设施库，不是主功能库，所以这里重点不是讲“如何围绕它单独开发系统”，而是把它的访问约定、和常见 bean util 的差异讲清楚。

## 添加依赖

```groovy
testImplementation "org.test-charm:bean-util:<version>"
```

## 这个库提供什么

- `BeanClass<T>`：对 Java 类型做统一封装，暴露构造器、泛型信息、属性 reader / writer 和类型判断。
- `PropertyReader` / `PropertyWriter`：面向“单个属性”的低层读写器。
- `Classes`：包扫描和可赋值类型查找工具，其他模块会拿它做扩展发现和 Spec 发现。

如果你正在使用 DAL 或 JFactory，其实已经间接在依赖这里的行为。

## bean 访问规则

最重要的是先搞清楚：**什么才算属性**。

### 读取支持什么

`bean-util` 支持读取：

- public field
- 标准 JavaBean getter，例如 `getName()`、`isEnabled()`

它**不**会把任意 public 无参方法都当成属性。那种更宽泛的“方法也可当属性”的体验，属于 DAL 的上层表达能力，不是 `bean-util` 本身的约定。

### 写入支持什么

`bean-util` 支持通过下面两类入口写值：

- public field
- setter，例如 `setName(...)`

如果目标本身是可写集合或数组，它也会把对应位置当成可写的数据槽。

## 直接属性 API 和点路径不是一回事

`BeanClass#getPropertyValue(...)` 和 `BeanClass#setPropertyValue(...)` 都是面向**单个属性名**的 API，不等价于 DAL 里的点路径导航。

这个区别很重要，因为很多人接触 `bean-util` 时，已经先见过 DAL 或 JFactory：

- DAL 在 property reader 之上构建了完整的数据导航语言。
- JFactory 在 property reader / writer 之上构建了嵌套对象创建。

所以上层看起来很像 `"customer.name"` 这种连续路径，但 `bean-util` 自身其实是更低一层的能力。

## 最小示例

```java
public class Customer {
    public String name;
}

public class Order {
    private Customer customer;

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
}

BeanClass<Order> orderType = BeanClass.create(Order.class);
BeanClass<Customer> customerType = BeanClass.create(Customer.class);

Order order = orderType.newInstance();
Customer customer = customerType.newInstance();

customerType.setPropertyValue(customer, "name", "Alice");
orderType.setPropertyValue(order, "customer", customer);

String name = (String) customerType.getPropertyValue(order.getCustomer(), "name");
```

这个例子刻意写得很“直给”，就是为了说明 `bean-util` 的边界：

1. `BeanClass` 是按类型拿到的。
2. 每次 `setPropertyValue(...)` 只负责当前对象上的一个属性。
3. 遇到嵌套对象时，要显式先拿到中间对象，再继续访问下一级属性，而不是直接传 `"customer.name"`。

如果你想直接写点路径，那通常应该交给 JFactory 或 DAL。

## 为什么它比很多 bean 工具更“宽松”

虽然 API 层面仍然是直接属性访问，但它背后的类型处理比较宽松：

- 会尽量保留泛型信息
- 会把集合和数组当成结构化数据，而不是黑盒容器
- 接口、代理类、动态形态对象只要暴露出可读 / 可写的 bean 风格属性，也能参与进来

也正因为这个底层足够灵活，上层的 DAL 和 JFactory 才能比较自然地处理对象图。

## 一般什么时候需要关心它

大多数使用者只有在下面这些问题出现时，才需要直接理解 `bean-util`：

- 为什么 JFactory 可以把这个对象图填出来？
- 为什么 DAL 能看到这个字段，但看不到那个方法？
- Spec 发现、扩展发现这类能力底层是怎么做的？

如果你没有遇到这些问题，通常可以把它视为整套框架的基础设施层。

## 仓库中的参考示例

如果你想看仓库里更具体的行为，可以从这些文件开始：

- [`src/test/java/org/testcharm/util/property/BeanClassTest.java`](src/test/java/org/testcharm/util/property/BeanClassTest.java)
- [`src/test/java/org/testcharm/util/property/PropertyReaderTest.java`](src/test/java/org/testcharm/util/property/PropertyReaderTest.java)
- [`src/test/java/org/testcharm/util/property/PropertyWriterTest.java`](src/test/java/org/testcharm/util/property/PropertyWriterTest.java)
- [`src/test/java/org/testcharm/util/PropertyChainTest.java`](src/test/java/org/testcharm/util/PropertyChainTest.java)

## 相关项目

- [DAL-java](../DAL-java/README.zh-CN.md)：在这里的 bean 规则之上构建完整的数据语言。
- [jfactory](../jfactory/README.zh-CN.md)：用同一套属性模型去创建和填充数据。

## 第三方开源声明

`bean-util` 将 `org.json`（JSON-java）做了 shaded 处理并重定位到 `org.testcharm.shaded.org.json`。随包提供的声明文件位于：

- [`src/main/resources/META-INF/NOTICE`](src/main/resources/META-INF/NOTICE)
- [`src/main/resources/META-INF/LICENSE-THIRD-PARTY`](src/main/resources/META-INF/LICENSE-THIRD-PARTY)
