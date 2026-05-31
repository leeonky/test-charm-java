# page-flow

[English](README.md)

`page-flow` 是 Test Charm 在 UI 场景上的扩展。它把 UI 操作也理解成数据操作，然后复用 [DAL-java](../DAL-java/README.zh-CN.md) 和 [jfactory](../jfactory/README.zh-CN.md)，让定位、操作、填表、断言都尽量保持简洁、直观。

这个模块还在持续完善，但当前抽象已经能很清楚地看出它的方向：让 UI 状态和 UI 行为也进入同一套数据优先的语言体系。

## 添加依赖

```groovy
testImplementation "org.test-charm:page-flow:<version>"
```

真正驱动浏览器时，还需要配合具体后端模块，例如 Selenium 或 Playwright 对应的 companion module。

## 核心抽象

### `PageFlow`

页面环境根对象，提供：

- 一个 `DAL` 实例，用于读取和断言
- 一个 `JFactory` 实例，用于组织结构化输入数据
- 一个共享对象表，用于挂载上下文对象

可以把 `PageFlow` 理解成“UI 自动化和 Test Charm 整体数据体系汇合的地方”。

### `Element`

表示单个 UI 元素，提供：

- 定位能力（`css`、`xpath`、`caption`、`placeholder`）
- 操作能力（`click`、`typeIn`、`clear`、`fillIn`）
- 状态读取（`text`、`tag`、`enabled`、`visible`、`dom`、`screenshot`）
- 基于 DAL 的求值与断言

这很重要，因为 Page Flow 并不希望 UI 断言回退成大量 imperative driver 代码，除非真的有必要。

### `Panel`

表示围绕某个元素构建出的更高层区域对象。它适合承载更贴近业务的页面行为，同时仍然保持 DAL 风格。

也就是说，你可以从“原始元素操作”逐步过渡到“业务语义化页面区域”，而不需要切换另一套心智模型。

## 用法长什么样

### 定位并断言

```dal
css: {
  '.target-str': str
  '.target-int': 100
}
```

这段表达式背后实际做的事是：

- `css` 表示“从当前页面根节点出发，用 CSS 选择器去定位”
- `'.target-str': str` 表示先找到 `.target-str`，再把它读取成 DAL 数据，并验证它等于 `str`
- `'.target-int': 100` 对 `.target-int` 做同样的事

如果不用 Page Flow，粗略等价的 Java 大概会写成：

```java
WebElement targetStr = driver.findElement(By.cssSelector(".target-str"));
assertEquals("str", targetStr.getText());

WebElement targetInt = driver.findElement(By.cssSelector(".target-int"));
assertEquals("100", targetInt.getText());
```

所以它的重点不只是少写几行，而是把“定位 + 断言”合成了一个结构化数据描述。

### 执行页面操作

```dal
css[.target].click
```

这句话拆开看就是：

- `css[.target]` 先定位出唯一的 `.target` 元素
- `.click` 再对这个元素执行点击

粗略等价的 Java 是：

```java
WebElement target = driver.findElement(By.cssSelector(".target"));
target.click();
```

这里的重点也不只是“代码更短”，而是 UI 交互依然保持在和其他模块一致的“数据导航 + 操作”模型里。

### 等待 UI 稳定

```dal
patience[1s].css[.target].text = hello
```

这句表达式其实同时做了等待、定位、取值和断言：

- `patience[1s]` 给这次查找一秒的等待窗口
- `css[.target]` 会在这个窗口里持续重试定位
- `.text` 读取元素文本
- `= hello` 断言最终文本值

粗略等价的 Java 会像这样：

```java
WebElement target = new WebDriverWait(driver, Duration.ofSeconds(1))
    .until(d -> d.findElement(By.cssSelector(".target")));
assertEquals("hello", target.getText());
```

UI 自动化里很多失败本质上都是时序问题。这种写法能让等待逻辑直接贴着断言写，而不必散落成一堆底层轮询代码。

### 用结构化数据填表

```dal
css: {
  'input[name=username]'.fillIn: alice
  'input[name=password]'.fillIn: secret
  'button[type=submit]'.click: {...}
}
```

这段块状表达式背后的动作是：

- 找到用户名输入框并填入 `alice`
- 找到密码输入框并填入 `secret`
- 找到提交按钮并点击

粗略等价的 Java 可以写成：

```java
driver.findElement(By.cssSelector("input[name=username]")).clear();
driver.findElement(By.cssSelector("input[name=username]")).sendKeys("alice");

driver.findElement(By.cssSelector("input[name=password]")).clear();
driver.findElement(By.cssSelector("input[name=password]")).sendKeys("secret");

driver.findElement(By.cssSelector("button[type=submit]")).click();
```

Page Flow 复用了 JFactory 风格的 collector，所以它还能继续往“直接拿结构化对象去填表”这个方向扩展，而不只是停留在逐字段 driver 操作。

这其实很符合它的整体设计：表单本身也是一种结构化数据界面，所以理应能直接吃结构化输入。

## 它和薄封装的区别

这个模块的目标不只是包一层 Selenium / Playwright API，而是让 UI 自动化也进入和其他测试数据一致的表达体系：

- 用 DAL 表达式定位元素
- 用 DAL 结构匹配断言 UI 状态
- 用 JFactory 构造输入数据
- 让 UI 自动化更接近业务数据，而不是低层脚本调用

这才是它真正想解决的问题。

## 仓库中的参考示例

仓库里比较适合继续阅读的示例在：

- [`src/test/resources/features/locate/find-one.feature`](src/test/resources/features/locate/find-one.feature)
- [`src/test/resources/features/locate/find-all.feature`](src/test/resources/features/locate/find-all.feature)
- [`src/test/resources/features/operation.feature`](src/test/resources/features/operation.feature)
- [`src/test/resources/features/form.feature`](src/test/resources/features/form.feature)

## 相关项目

- [DAL-java](../DAL-java/README.zh-CN.md)：驱动定位、读取和断言。
- [DAL-extension-basic](../DAL-extension-basic/README.zh-CN.md)：其中的异步和文件能力在 UI 场景也常常有用。
- `page-flow-selenium` 与 `page-flow-playwright`：具体浏览器后端实现。
