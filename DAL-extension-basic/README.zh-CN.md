# DAL-extension-basic

[English](README.md)

`DAL-extension-basic` 是 [DAL-java](../DAL-java/README.zh-CN.md) 最常用的一组基础扩展。它把 DAL 真正带进日常自动化场景里最常遇到的几类数据：JSON、YAML、文件、路径、ZIP、二进制、字符串 diff、列表辅助，以及 `::eventually`、`::await` 这类异步断言。

没有这个模块时，DAL 依然是一门很有表现力的数据语言；加上它之后，DAL 才更像“一门真正覆盖测试中常见数据来源的统一语言”。

## 添加依赖

```groovy
testImplementation "org.test-charm:DAL-extension-basic:<version>"
```

## 扩展是怎么生效的

这里要分两种常见使用方式，因为很多人其实只会用到第一种。

### 1. 直接加依赖，然后走标准 DAL 入口

如果你平时用的是这些常规入口：

- `DAL.dal()`
- `DAL.dal("SomeName")`
- `Assertions.expect(...)`

那么只要 `DAL-extension-basic` 在 classpath 上，扩展就会被自动发现并装载。

也就是说，对大多数“直接用默认 DAL 实例”的场景来说，**加依赖通常就已经够了**。

### 2. 自己手动创建 DAL 实例

如果你是更手工地组装 DAL，那就应该把扩展加载写出来：

```java
DAL dal = new DAL("manual").extend();
```

这种写法适合你需要一个专门的命名实例，或者要自己装配一套定制运行时。

## 它补齐了什么

| 领域 | 实际获得的能力 |
| --- | --- |
| 文本格式 | 把 JSON、YAML 字符串和文本块直接解析成 DAL 数据 |
| 文件和路径 | 把目录、文件、路径对象当作 DAL 数据树来遍历和断言 |
| ZIP 与二进制 | 检查压缩包条目、字节、字符串和十六进制表示 |
| 字符串 | 失败时得到更可读的 diff |
| 异步 | 用 `::eventually`、`::await` 表达重试式断言 |
| 列表 | 使用过滤、top 等常用列表辅助能力 |

## 常见用法

### 直接解析 JSON / YAML

````text
'{"key":"value"}'.json = { key: value }

```yaml
key: value
```
= { key: value }
````

这个例子的重点不是“能解析 JSON / YAML”这么简单，而是：

- 左边依然只是 DAL 输入
- JSON 和 YAML 可以写在最适合阅读的位置
- 一旦解析完成，它们就和普通 DAL 对象没有区别

因此 JSON 请求体、YAML 配置、Java 对象，最终都能回到同一种结构化断言方式上。

### 把文件和目录当成数据断言

```java
Path root = Paths.get("src/test/resources/example-dir");

expect(root).should("""
  : {
    a.json: { key: value }
    b.yaml: { key: VALUE }
  }
""");
```

这个例子很关键，因为它展示的不是“DAL 会读文件”，而是“目录会变成一棵可以导航的数据树”：

- 文件名变成属性
- 子目录变成嵌套对象
- 能识别的文本格式还能继续被结构化校验

这样你就不必在测试里来回切换 `Files.readString(...)`、JSON 解析器、YAML 解析器和断言代码。

### 检查 ZIP 内容

```dal
unzip: {
  'file1.txt': { string: hello }
  'file2.txt': { string: world }
}
```

这里的 `unzip` 不是单纯把字节解开，而是把压缩包变成结构化 DAL 数据。这在校验导出包、报告文件、打包产物时非常实用。

### 处理异步 / 最终一致性

```dal
::eventually::in(5s)::every(200ms): {
  status: READY
}
```

这部分是这个模块最实用的能力之一。自动化测试里很多失败并不是“永远错了”，而是“当前还没稳定”。`::eventually` 让重试逻辑直接和断言写在一起。

而 `::await` 更适合那种“先等某个子表达式变得可用，再继续往下断言”的场景。

### 把 HTTP 响应直接当结构化数据断言

当它和 [RESTful-cucumber](../RESTful-cucumber/README.zh-CN.md) 搭配使用时，响应对象也可以继续停留在普通 DAL 断言里：

```dal
: {
  code= 200
  body.json= {
    code: S01
    customer: {
      name: Alice
    }
  }
}
```

这种写法的价值在于，它把原本分散的几层处理合到了一起：

- 不需要单独写 JSON 解析步骤
- 不需要先抽出 body、再写一层字段断言 helper
- 同一条 DAL 断言里同时表达协议层和载荷层的期望

所以一个 HTTP 响应，在这里会自然地变成和对象、Map、文件、目录同一类的结构化数据。

### 一句话完成 ZIP 响应体解压验证

同样的思路也适用于二进制载荷。如果某个 API 返回的是 ZIP 文件，DAL 可以直接解压并继续按结构验证：

```dal
: {
  code= 200
  body.unzip: {
    'orders/1.json'.json: {
      id: 1
      code: SN1
    }
    'orders/2.json'.json: {
      id: 2
      code: SN2
    }
  }
}
```

这里真正节省下来的不只是代码行数，而是你可以一直停留在“我关心什么数据”的层次：

- 响应成功
- body 是一个 ZIP
- ZIP 里包含预期文件
- 每个 JSON 文件都具有预期结构和值

如果不用这种方式，等价逻辑通常会散成字节处理、遍历压缩包、抽取文件、解析 JSON，再加上多层断言代码。

## 什么时候它最值

当你的断言开始离开“普通内存对象”，进入这些自动化测试真正常见的数据时，这个模块就会非常值：

- 请求 / 响应载荷
- 配置文件
- ZIP 导出文件
- 一整目录的生成产物
- 最终一致性的状态变化

这也是为什么它通常会成为 DAL 生态里最先加上的 companion module 之一。

## 仓库中的参考示例

如果你想继续看仓库里的实际例子，可以从这些文件开始：

- [`src/test/resources/features/text/json.feature`](src/test/resources/features/text/json.feature)
- [`src/test/resources/features/text/yaml.feature`](src/test/resources/features/text/yaml.feature)
- [`src/test/resources/features/dir-files/file.feature`](src/test/resources/features/dir-files/file.feature)
- [`src/test/resources/features/dir-files/path.feature`](src/test/resources/features/dir-files/path.feature)
- [`src/test/resources/features/dir-files/zip.feature`](src/test/resources/features/dir-files/zip.feature)
- [`src/test/resources/features/async/async.feature`](src/test/resources/features/async/async.feature)

## 相关项目

- [DAL-java](../DAL-java/README.zh-CN.md)：核心语法和运行时。
- [DAL-extension-jdbc](../DAL-extension-jdbc/README.zh-CN.md)：数据库视角的 DAL 扩展。
- [page-flow](../page-flow/README.zh-CN.md)：把同样的 DAL 风格带到 UI 状态和 UI 操作里。
