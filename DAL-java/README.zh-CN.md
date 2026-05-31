# DAL-java

[English](README.md)

`DAL-java` 是 Test Charm 里的核心 Data Assertion Language 实现。它位于 JSON 和通用编程语言之间：表达能力比 JSON 强很多，但又刻意比通用语言更小、更适合手写自动化代码。

它的目标很实际：让人们能用一种更像“围绕数据沟通”的写法，去描述测试数据、导航数据、转换数据和验证数据。所以 DAL 对对象、列表、表格、文本块、schema，以及文件、数据库、UI 这类扩展数据源都很友好。

这里有一个边界最好一开始就讲清楚：**DAL 不是编程语言**。它是一门数据描述语言。它只在确实有助于更自然地读取和验证数据时，才保留极少量带“计算感”的表达形式。

## 添加依赖

```groovy
testImplementation "org.test-charm:DAL-java:<version>"
```

## 大多数人是怎么开始用它的

DAL 通常有两个入口，而它们分别对应两种最常见的工作。

### 1. 断言入口

当你的目标是验证数据时，用 `Assertions.expect(...)`：

```java
import static org.testcharm.dal.Assertions.expect;

expect(order).should("""
  : {
    customer: {
      name: 'Alice'
    }
    items: | name   | quantity |
           | Pencil | 2        |
  }
""");
```

这段看起来很短，但它同时做了几件事：

- 以一个 Java 对象作为根值
- 导航到嵌套属性
- 只匹配当前场景真正关心的字段
- 对集合部分使用表格，因为这里表格最直观

这正是 DAL 最典型的使用方式。

### 2. 求值入口

当你想把 DAL 当成一个紧凑的数据查询语言来用时，直接用 `DAL`：

```java
DAL dal = DAL.dal();

String customer = dal.evaluate(order, "customer.name");
List<Integer> ids = dal.evaluate(order, "items.id[]");
```

`evaluateAll(...)` 会执行多条表达式并返回结果列表。这个入口适合“我要拿值出来继续用”，而不只是“我要断言它”。

## 心智模型

每条 DAL 表达式都运行在一个根值之上，这个根值可以是：

- Java 对象
- `Map`、`List`、数组
- 任意通过访问器注册进来的自定义类型

在这个根值之上，DAL 提供：

- 属性和元素访问
- 值比较
- 部分结构匹配
- 列表映射
- 表格验证
- schema 验证
- 通过扩展访问更领域化的数据源

只要记住“一个根值，一套统一结构化语言”，DAL 后面的很多写法都会自然很多。

## 核心语法

### 属性访问

DAL 能读取：

- public getter
- public field
- public 无参方法
- `Map` entry
- 自定义 property accessor
- 注册过的静态方法扩展

例如：

```dal
customer.name
items[0].quantity
['display name']
'hello'.length
```

开头的 `.` 是可选的，所以根上的 `name` 和 `.name` 是一回事。

这里真正重要的不只是“它能读这些东西”，而是 DAL 把它们统一成了一套导航语言。所以对象、Map、列表、扩展类型都能沿用同样的写法。

### 用有限的“方法柯里化”去取数

DAL 的确支持少量方法调用，但这件事应该理解成**取数据**，而不是“DAL 现在也能编程了”。

例如，如果某个图片类暴露的是 `getPixel(int x, int y)` 这种方法，那么 DAL 可以用一种柯里化的属性风格去验证它：

```dal
img.getPixel: {
  '0': {
    '0': '#ffffff'
    '1': '#ffff00'
  }
  '1': {
    '0': '#00ffff'
    '1': '#00ff00'
  }
}
```

这段表达式可以按下面理解：

- 先从 `img` 取到 `getPixel` 这个访问入口
- 再传入第一维坐标
- 再传入第二维坐标
- 最后比较返回出来的像素值

这正是 DAL 支持方法调用的初衷：有些“获取数据”的 API 天生就不是 getter，而是需要坐标、索引或其他维度参数。

这个能力是刻意收着的。它存在的目的，是让 DAL 能够访问“带参数的数据读取接口”，而不是把业务逻辑搬进 DAL 表达式里。

### 列表访问与映射

```dal
items[0]
items[-1]
items.size
items.id[]
products.catalog[].sub.value.string
```

属性后面的 `[]` 表示“把这个属性映射到当前列表的每个元素上”。DAL 不会做隐式列表映射，必须显式写出来。

这种显式性很重要，因为当数据结构变复杂时，你能一眼看出列表映射究竟发生在什么位置。

### 相等与匹配

- `=` 表示严格相等
- `:` 表示宽松 / 结构化匹配

典型例子：

```dal
= 42
: { id: 1, name: Alice }
items: [1 2 3]
```

当你希望多余字段也算失败时，用 `=`；当你只想聚焦当前场景有意义的那部分结构时，用 `:`。

这也是 DAL 在大型测试集里仍然可读的原因之一：严格度是显式写在断言里的，而不是藏在某个断言 helper 约定里。

### 表格

表格是 DAL 最有代表性的集合断言形式之一：

```dal
: | name   | age |
  | 'Tom'  | 10  |
  | 'Lucy' | 15  |
```

当数据本来就适合以行列方式理解时，这种写法会非常自然。

很多时候，表格正是 DAL 看起来比 JSON 式断言更接近“人手写的验证语言”的原因。

### Schema

Schema 用来同时验证结构和类型 / 约束：

```dal
is OrderSchema
```

Schema 字段可以定义期望类型、默认值、嵌套结构、是否允许为空等规则。它适合“这里描述的是一类可复用的数据契约”，而不只是一次性的示例对象。

### 字面量与文本块

DAL 支持：

- 带 Java 风格后缀的数值字面量，例如 `L`、`F`、`BD`、`BI`
- 单引号 / 双引号字符串
- fenced 或反引号风格的多行文本块
- 在验证语境下使用正则

这些形式重要，是因为真实自动化测试要描述的数据远不只是平铺的 JSON 标量。

## Java API 表面

最常用的入口是：

```java
<T> T evaluate(Object input, String expression)
<T> List<T> evaluateAll(Object input, String expressions)
Assertions.expect(input).should(expression)
Assertions.expectRun(supplier).should(expression)
```

`Assertions.expectRun(...)` 特别适合那种“实际值是延迟产生的，或者求值本身可能抛异常”的场景。

## 扩展 DAL

DAL 的设计目标之一，就是把不同数据源统一到同一种语言里。

你可以注册：

- 自定义 property accessor
- 自定义 list accessor
- 静态方法扩展
- schema 类型
- 更友好的 dumper

也可以通过 `dal.extend(...)` 自动装载扩展模块。文件、JDBC、JFactory、UI 这些 companion module 就是通过这个机制接进来的。

这个扩展模型非常关键，因为整个 Test Charm 栈的目标从来不只是“断言 Java 对象”，而是让各种不同来源的数据都能说同一种语言。

## 它在实践中的价值

DAL 最强的地方，是你可以用同一种记号去完成这些事情：

- 验证内存中的 Java 对象
- 读取嵌套 JSON 风格载荷
- 用表格描述数据集
- 断言数据库快照
- 检查文件和 ZIP 内容
- 校验 UI 状态

“一种语言面对所有数据”这一点，正是整套 Test Charm 生态围绕 DAL 来构建的原因。

## 仓库中的参考示例

如果你想通过仓库中的例子继续学语法，这些文件很适合作为入口：

- [`src/test/resources/features/doc/access/2-access-property.feature`](src/test/resources/features/doc/access/2-access-property.feature)
- [`src/test/resources/features/dal/method/currying.feature`](src/test/resources/features/dal/method/currying.feature)
- [`src/test/resources/features/dal/property/list-mapping.feature`](src/test/resources/features/dal/property/list-mapping.feature)
- [`src/test/resources/features/dal/verification/object.feature`](src/test/resources/features/dal/verification/object.feature)
- [`src/test/resources/features/dal/verification/list.feature`](src/test/resources/features/dal/verification/list.feature)
- [`src/test/resources/features/dal/verification/table/basic.feature`](src/test/resources/features/dal/verification/table/basic.feature)
- [`src/test/resources/features/dal/verification/schema/single.feature`](src/test/resources/features/dal/verification/schema/single.feature)

## 相关项目

- [DAL-extension-basic](../DAL-extension-basic/README.zh-CN.md)：补充文件、文本、异步、字符串、二进制、ZIP 等能力。
- [DAL-extension-jdbc](../DAL-extension-jdbc/README.zh-CN.md)：把数据库表关系暴露成 DAL 数据。
- [DAL-extension-jfactory](../DAL-extension-jfactory/README.zh-CN.md)：把 JFactory repository 数据暴露给 DAL。
- [jfactory](../jfactory/README.zh-CN.md)：经常直接使用 DAL 风格语法来描述“要创建什么数据”。
