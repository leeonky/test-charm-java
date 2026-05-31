# DAL-extension-jfactory

[English](README.md)

`DAL-extension-jfactory` 是 [DAL-java](../DAL-java/README.zh-CN.md) 和 [jfactory](../jfactory/README.zh-CN.md) 之间的桥接层。它会把 JFactory repository 中、按 spec 命名的数据集合暴露成 DAL 属性，让 repository 查询也进入同一套断言语言。

这个模块本身不大，但位置非常关键：它把“用 JFactory 创建数据”和“用 DAL 验证数据”真正闭环起来了。

## 添加依赖

```groovy
testImplementation "org.test-charm:DAL-extension-jfactory:<version>"
```

## 使用前先确认两件事

有两个前提非常重要：

1. 相关 Spec 必须已经注册进 `JFactory`
2. 这个 `JFactory` 必须带着一个真的能返回数据的 repository

缺了这两点，DAL 侧就不会有真正有意义的 spec 数据集合可供读取。

## 它具体做了什么

扩展生效之后，一个 `JFactory` 实例会表现得像一个 DAL 对象，而它的属性名就是已注册的 spec 名称。

如果 `Orders` 是一个已注册 spec，那么你就可以写：

```dal
: {
  Orders: [{
    id= 1
    code= SN1
  }]
}
```

它底层做的事情，本质上等价于 `jFactory.spec("Orders").queryAll()`，然后把查询结果暴露成一个 DAL 列表。

所以这个模块用起来会很自然，因为它并没有再发明一套查询语言，而只是让 DAL 能直接看见 JFactory repository 里的结构化数据。

## 为什么它有价值

这个模块让 JFactory repository 数据在 DAL 里变得非常自然：

- 按 spec 名字查询，而不是写 imperative repository 代码
- 用普通 DAL 的 dump 能力直接看 repository 状态
- 可以继续使用列表断言、表格断言、结构匹配
- repository 状态异步变化时，也能自然结合 `::eventually`

因为结果暴露成的是 adaptive list，异步断言写起来也很顺：

```dal
Orders::eventually: {
  id= 1
  code= SN1
}
```

adaptive list 可以理解成 DAL 里一种“结果还保持列表语义，但又能按需要继续适配 / 重新读取”的集合抽象。实际效果上它意味着：

- 当你需要按集合验证时，它就是一个列表
- 当结果里恰好只有一个元素时，DAL 也可以把它按单对象来继续访问
- `::eventually` 这类元操作可以很自然地反复重新检查它

所以 `Orders: [{...}]` 和更短的 `Orders: {...}` 都能成立，异步 repository 断言也不会变得很啰嗦。

这让它在一些集成测试场景里尤其好用，例如数据会由后台任务、异步处理流程稍后写入 repository。

## 典型配置

```java
JFactory jFactory = new JFactory(repository);
jFactory.register(Orders.class);

expect(jFactory).should("""
  : {
    Orders: [{
      code= SN1
    }]
  }
""");
```

这段代码背后的含义是：

- 先注册 Spec，让 JFactory 知道 `Orders` 这个名字
- 让 repository 里的数据通过这个 spec 可查询
- 再让 DAL 把 `Orders` 当成 `JFactory` 根对象上的一个属性去读取

这就是这个模块最核心的工作流。

## 最适合什么场景

这个模块最适合下面这种组合：

- JFactory 已经负责你的数据创建和 repository 抽象
- DAL 已经是你的断言语言
- 你希望 repository 校验看起来和其他所有数据校验保持一致

如果你的测试本来就已经在用“spec 名 + 结构化数据”这套语言，这个模块能减少很多中间胶水代码。

## 仓库中的参考示例

仓库里最直接的参考示例在：

- [`src/test/resources/features/query-data.feature`](src/test/resources/features/query-data.feature)

## 相关项目

- [jfactory](../jfactory/README.zh-CN.md)：Spec、Trait 和 repository 能力的来源。
- [DAL-java](../DAL-java/README.zh-CN.md)：核心语法和断言模型。
- [jfactory-repo-jpa](../jfactory-repo-jpa/README.zh-CN.md)：这个扩展常见配套的 repository 实现。
