# RESTful-cucumber

[English](README.md)

`RESTful-cucumber` 封装了 Web API 测试里最常见的一组 Cucumber Step，并把它们和 [DAL-java](../DAL-java/README.zh-CN.md)、[jfactory](../jfactory/README.zh-CN.md) 结合起来，让“发请求、构造请求体、验证响应”都能保持数据化、紧凑和直观。

这个模块最有价值的地方，在于它让 API 场景保持声明式，而不是退回成一堆 HTTP client 代码再加上 JSON 断言代码。

## 添加依赖

```groovy
testImplementation "org.test-charm:RESTful-cucumber:<version>"
```

## 它提供什么

核心类型是 `RestfulStep`，主要支持：

- `GET`、`DELETE`、`POST`、`PUT`、`PATCH`
- 从 URL 或 DAL / 对象风格 doc string 中生成查询参数
- 用结构化数据表达请求头
- 处理 JSON、纯文本、二进制、multipart form-data 请求体
- 用 DAL 断言响应
- 可选地用 JFactory Spec / Trait 生成请求体

## 一般怎么做配置

在真实项目里，围绕 `RestfulStep` 通常会配置三类东西：

1. base URL
2. 可选的 `JFactory`，用于 spec-backed 请求体
3. 某些 API 特有的 body writer 或默认文档类型

也正因为这些传输层细节通常是一次性配置的，feature 文件才能主要围绕请求 / 响应数据本身来写。

## 基础请求 Step

```gherkin
When GET "/orders"

When GET "/orders":
  """
  {
    "customer": "Alice"
  }
  """

When POST "/orders":
  """json
  {
    "code": "S01"
  }
  """
```

这些例子真正重要的点在于：

- HTTP 方法直接体现在 Step 里
- 请求路径保持很短
- 查询参数和请求体都可以按“数据”来写，而不是手工拼接字符串

所以 feature 文件关注的是业务输入，而不是低层 HTTP client 装配。

## 响应断言

响应验证也走 DAL：

```gherkin
Then response should be:
  """
  : {
    status: 200
    body.json: {
      code: S01
    }
  }
  """
```

这是这个模块很强的一点：请求和响应的表达风格是对称的。

你不需要一个 helper 解析 JSON、另一个 helper 检查 status、再写第三套 ad hoc 代码去比字段，而是可以一直待在同一种断言语言里。

## 用 JFactory 生成请求体

这个模块和 JFactory 的结合也非常实用，可以直接从 Spec / Trait 生成请求体：

```gherkin
When POST "LoginRequest" "/login":
  """
  {
    "username": "admin"
  }
  """
```

这里内联文档会覆盖或补充 Spec 生成的默认体。它的价值在于把这两件事结合起来了：

- JFactory 提供真实、可复用的默认数据
- feature 文件只写当前场景真正想改的那部分

Trait 当然也一样可以用：

```gherkin
When POST "WrongPassword LoginRequest" "/login":
  """
  {
    "username": "admin"
  }
  """
```

这样请求体既不会太长，又不用把所有字段在每个场景里都重复一遍。

## Multipart 与文件上传

这个模块还可以用结构化数据构造 multipart 请求，包括普通字段和文件 / 虚拟文件：

```gherkin
When POST form "/files":
  """
  : {
    key: value
    file(ATextFile): {
      name= u.txt
      content= hello-world
    }
  }
  """
```

这意味着即使是混合表单载荷，也依然能继续停留在统一的数据描述风格里，而不用掉回自定义 multipart helper。

## 默认内容类型

`RestfulStep` 支持配置默认文档类型。这很重要，因为同一段 doc string 可能需要被解释成：

- 原始文本
- 求值后得到 JSON 风格对象的 DAL
- 二进制字节流
- multipart form payload

也正是这个机制，让 feature 文件既能保持简洁，又不会失去对真实 HTTP 载荷语义的控制。

## 仓库中的参考示例

仓库里比较值得继续看的例子包括：

- [`src/test/resources/features/module-safe/restful.feature`](src/test/resources/features/module-safe/restful.feature)
- [`src/test/resources/features/module-safe/request.feature`](src/test/resources/features/module-safe/request.feature)
- [`src/test/resources/features/module-safe/request-with-body/basic-step.feature`](src/test/resources/features/module-safe/request-with-body/basic-step.feature)
- [`src/test/resources/features/module-safe/request-with-body/spec-in-step.feature`](src/test/resources/features/module-safe/request-with-body/spec-in-step.feature)
- [`src/test/resources/features/module-safe/jfactory.feature`](src/test/resources/features/module-safe/jfactory.feature)

## 相关项目

- [DAL-java](../DAL-java/README.zh-CN.md)：请求和响应断言都以 DAL 表达。
- [jfactory](../jfactory/README.zh-CN.md)：请求体可以从 Spec 和 Trait 生成。
- [jfactory-cucumber](../jfactory-cucumber/README.zh-CN.md)：在 Cucumber 场景里承担互补的数据准备角色。
