# cucumber-swarm

**辅助文档：中文。英文主文档见 [README.md](README.md)。**

`cucumber-swarm` 让 Cucumber 以 **master / worker** 的方式运行。  
它的出发点是解决这样一类端到端测试问题：希望提升执行速度，但又不能把 Cucumber 内置的多线程并发，直接施加到同一套被测系统和环境上。

## 概览

这个项目的出发点很实际。Cucumber 内置并发主要基于多线程，但很多端到端测试面对的是同一套被测系统和同一套测试环境，
这类环境往往并不适合承载多个测试稳定并发执行。对这类场景来说，真正可行的提速方式通常不是在同一套环境里继续增加线程数，
而是准备**多套独立但同构的测试环境**，例如多套 docker-compose 部署，再把场景分布式地调度到这些环境中执行。

也正因为如此，问题的重点不再是“如何在一个 runtime 里开更多线程”，而是“如何在多个隔离环境之间分发测试，同时仍然把结
果视为一次完整的 Cucumber 运行”。`cucumber-swarm` 的做法，是把并发策略从“单进程多线程”改成“多环境分发执行，再统一汇
总结果”。其中 **master** 负责规划和协调，**worker** 在各自的 runtime 中执行场景，并把执行信息回传给 master；随后
master 再把这些结果组织成一条聚合后的运行结果流，供 summary 和 master 侧插件消费。

相比原始 Cucumber，这种方式保留了“一次运行”的使用心智，但改变了工作实际发生的位置。因此，`cucumber-swarm` 特别适合那
些希望集中调度场景、把执行分布到本地或远程 worker、同时仍然保留统一结果出口和汇总能力的测试场景。

### 哪些地方仍然像普通 Cucumber

`cucumber-swarm` 仍然保留了大多数使用者最关心的行为，包括标准的 scenario / step 结果，
passed / failed / skipped / pending / undefined / ambiguous 等执行状态，undefined step 时的 snippet 提示，
对 feature 目录和单个 feature 文件 target 的支持，以及最终统一的成功 / 失败退出码。

### 哪些地方已经不一样了

变化并不只是“多了几个进程”，而是运行责任发生了转移：

| 维度    | 原始 Cucumber      | cucumber-swarm               |
|-------|------------------|------------------------------|
| 场景调度  | 本地 runtime 自己决定  | 由 master 统一分配                |
| 场景执行  | 规划和执行都在同一进程      | master 规划，worker 执行          |
| 初始化   | 一套 runtime 初始化一次 | 每个 worker 都会初始化自己的 runtime   |
| 插件观察点 | 观察本地原始执行流        | 观察 master 侧聚合后的结果流           |
| 输出归属  | 执行进程自己输出         | 最终 summary / 结果由 master 统一负责 |
| 远程执行  | 不是默认模型           | 是运行方式的一部分                    |

## 能不能把它当原始 Cucumber 来用？

**很多场景可以，但不能无条件假设完全等价。**

如果你的关注点主要是 scenario / step 生命周期结果、总体 summary，以及最终成功 / 失败状态，那么把它理解成“分布式执行，
但最终仍由 master 汇总成一次运行”通常是成立的。不过，它并不是对所有单进程假设都完全透明，因此是否可以把它近似看作原始
Cucumber，仍然要结合具体工作流来判断。

### 关于 plugin 兼容性

更稳妥的理解是：`cucumber-swarm` 更适合那些关心场景执行结果、步骤结果和最终汇总结果的插件；如果插件依赖进程本地行为、
worker 本地 stdout，或者依赖完整的内部事件流，就需要单独验证。

根据当前实现和测试，master 总会加入 `MasterPlugin`，worker 总会加入 `WorkerForwardingPlugin`，而使用者自己传入的
`--plugin` 仍然只挂在 master 侧。worker 的执行结果会以 test-case / test-step 生命周期信息为主回传给 master，同时还会
转发一部分 Cucumber message payload，但不是所有 worker 本地事件都会原样转发。换句话说，如果插件只关心聚合后的运行结果，
通常更容易适配；如果插件要求自己必须直接挂在真正执行步骤的那个 runtime 上，就应当谨慎验证。

### 关于输出兼容性

一个非常实际的差异是 **stdout 行为**。在 **本地 worker 模式** 下，步骤里的 `System.out.println(...)` 会出现在最终运
行输出中；而在 **远程 worker 模式** 下，最终 stdout 会显示 master summary，但 worker 里的
`System.out.println(...)` 不会以同样方式被并入最终 stdout。因此，如果你的工作流依赖 worker 的控制台输出直接进入主终端
输出，远程模式就需要额外评估。

### 关于初始化兼容性

因为真正执行场景的是 worker，所以 worker 环境必须具备完整运行条件，包括 feature target、glue、hooks、运行依赖，以及
启动 worker 所需的 JVM 或进程参数。这一点在远程模式下尤其重要。master 可以负责协调运行，但 worker 仍然必须是一个能够独
立执行 Cucumber 的环境。

## 分布式执行会带来哪些实际影响

### 1. 执行和初始化被拆开了

master 不执行步骤，执行发生在 worker。

因此所有和步骤执行强相关的东西，都发生在 worker runtime 中，包括：

- glue 加载
- hook 执行
- 步骤执行时需要的对象初始化
- worker 本地 stdout / stderr 行为

### 2. master 是最终汇总点

master 才是统一理解“这次测试跑得怎么样”的地方。

因此最适合放在 master 侧处理的是：

- 最终 summary
- 集中式结果判断
- master 侧插件消费
- 启动超时与 worker 可用性管理

### 3. 远程模式本质上也是部署问题

远程模式并不只是命令行层面的一个变体，它本质上也是部署和调度方式的一部分。你不仅是在决定 worker 如何被启动，也是在决定
它运行在哪个环境中，以及那个环境如何与 master 建立连接。

这份 README 里的示例使用 `java`，是因为这种形式最底层，也与模块自身测试中的调用方式一致。但在真实项目中，远程 worker
通常并不是直接以 JVM 命令行的方式启动。更常见的做法包括：

- 通过 `docker-compose exec` 进入某个测试容器，在其中执行 Gradle 或 Maven 的 Cucumber task
- 通过 `ssh` 登录另一台机器，在远端执行项目约定的测试命令
- 用脚本封装 worker 的准备过程，再由脚本调用 Cucumber

因此，`--remote-worker-launcher` 更适合被理解为“远程执行入口命令”，而不是一个天然等同于 `java` 的参数。无论这个入口是
脚本、容器命令、SSH 命令，还是直接的 JVM 调用，使用者通常都需要考虑运行时准备、构建任务选择、日志配置、环境相关参数，以
及 worker 回连 master 的 host / port 可达性。

## 如何运行 cucumber-swarm

### 本地 worker 模式

```bash
java ... org.testcharm.cucumber.swarm.Main \
  --glue steps \
  features
```

效果：

- master 启动
- 默认自动创建一个本地 worker
- worker 向 master 请求场景执行
- 最终退出码由 master 统一产出

这个例子主要用于说明执行模型。在实际工程里，同样的调用往往会再包一层 Gradle 或 Maven task，而不一定直接通过 `java`
执行。

### 远程 worker 模式

从最底层的形式看，远程 worker 启动可以写成这样：

```bash
java ... org.testcharm.cucumber.swarm.Main \
  --swarm-port 20000 \
  --disable-local-worker \
  --remote-worker-count 1 \
  --remote-options-json '["-Djava.util.logging.config.file=/tmp/cucumber/logging.properties","-cp","...","org.testcharm.cucumber.swarm.Main"]' \
  --remote-worker-launcher java \
  --glue steps \
  features
```

含义是：

- `--remote-worker-launcher` 指定启动 worker 的入口命令
- `--remote-options-json` 指定固定的启动前缀
- swarm 自动追加 worker id、swarm 连接参数和归一化后的 feature target

在真实项目中，很多团队会把 `--remote-worker-launcher` 指向脚本、`docker-compose` 命令或 `ssh` 命令，再由这些入口在目
标环境里执行项目平时使用的 Gradle 或 Maven Cucumber task。

## CLI 参数

| 参数                                   | 默认值         | 作用                           |
|--------------------------------------|-------------|------------------------------|
| `--remote-worker-launcher <bin>`     | 无           | 启动远程 worker 进程的可执行程序         |
| `--disable-local-worker`             | 关闭          | 禁用默认本地 in-process worker     |
| `--swarm-host <host>`                | `localhost` | 远程 worker 回连 master 使用的 host |
| `--swarm-port <port>`                | `10083`     | master 协调端口                  |
| `--worker-timeout <seconds>`         | `5`         | master 等待 worker 完成启动并进入可用状态的时间 |
| `--remote-worker-count <n>`          | `0`         | 启动远程 worker 的数量              |
| `--remote-options-json <json-array>` | `[]`        | 插入到 launcher 后面的额外启动参数       |
| `--worker-id <id>`                   | 无           | 标记某个进程为 worker               |

## 使用上的注意事项与限制

### 线程数会被固定为 1

当前参数预处理会强制：

```bash
--threads 1
```

master 和 worker 都一样。

### plugin 路由是刻意设计过的

`cucumber-swarm` 并不是让每个进程都像普通 Cucumber 一样充当完整 plugin 宿主。

- **master** 持有聚合后的事件流和最终报告出口
- **worker** 负责执行和转发
- worker 侧的 forwarding plugin 会被强制加入，以便 master 重新组装一条连贯的运行结果流

这也是为什么它通常更适合面向生命周期结果的报告，而不是强依赖“本地原始执行细节”的插件。

### 远程 worker 的 feature target 会被归一化

远程 worker 支持：

- feature 目录
- 单个 feature 文件
- 带行号的 feature 文件
- 多个 target
- 绝对路径会在生成远程 worker 参数时自动转为相对路径

### worker 启动采用显式协调机制

在 master 开始分配场景之前，它会先等待至少一个 worker 完成启动并声明自己已经可以接收执行任务。`--worker-timeout`
控制的就是这段初始等待时间。如果在这段时间内没有任何 worker 进入可用状态，运行会停止，并给出：

```text
No worker available after waiting for <n> seconds
```

这一点在本地和远程模式下都成立，但在远程模式下尤其重要，因为远端启动往往还会受到容器调度、远程命令执行、网络连通性或构建
工具启动时间的影响。

### 远程 worker 必须提供明确的启动命令

如果你要求启动远程 worker，就必须同时提供用于启动它们的命令。否则 swarm 无法实际拉起远程 worker 进程，运行会停止，并给
出：

```text
Missing option --remote-worker-launcher
```

## 一些有助于理解上面内容的内部实现说明

这里不展开成设计文档，只保留和使用判断有关的实现信息。

### 协调协议

master 会暴露 3 个接口：

| 方法     | 路径        | 用途                       |
|--------|-----------|--------------------------|
| `GET`  | `/pickle` | worker 请求下一个场景           |
| `POST` | `/events` | worker 回传执行信息            |
| `POST` | `/ready`  | worker 确认自身已完成启动并可以接收场景 |

worker 会通过 `/pickle` 持续请求待执行场景，直到没有剩余工作；而单独设置 `/ready` 这一步，则是为了让 master 能区分“进程已被拉起”
和“运行时已初始化完成并可实际执行场景”这两个状态。

### worker 回传的内容

在 plugin-event 层面，当前主要回传：

- `io.cucumber.plugin.event.TestCaseStarted`
- `io.cucumber.plugin.event.TestStepStarted`
- `io.cucumber.plugin.event.TestStepFinished`
- `io.cucumber.plugin.event.TestCaseFinished`

在 Cucumber message 层面，当前会转发的 envelope payload 包括：

- `io.cucumber.messages.types.TestCase`
- `io.cucumber.messages.types.TestCaseStarted`
- `io.cucumber.messages.types.TestStepStarted`
- `io.cucumber.messages.types.TestStepFinished`
- `io.cucumber.messages.types.TestCaseFinished`

这也是为什么 `cucumber-swarm` 能在 master 侧继续呈现一个有意义的聚合运行结果；但也正因为它是“转发 + 聚合”，plugin
兼容性不应被默认假设，而应结合分布式运行模型来判断。

### 事件传输是排队并在退出前刷完的

worker 不会在每一步执行时都同步阻塞地立刻 POST 事件。  
它会先把事件放进队列，由专门的转发线程发送给 master，并在 worker 退出前等待队列刷完。

这样可以减少步骤执行和 HTTP 传输的直接耦合，但也进一步说明它不是“本地插件直接观察原始执行”的模型。

### 为什么会有“回映射”

worker 回传的不是进程内活对象，而是序列化后的执行信息。

master 会把这些信息重新组装成聚合后的运行对象，以便：

- 恢复 scenario / step 身份
- 保持路径映射一致
- 让 master 侧 summary 和插件看到一条连贯的运行结果流

### 远端异常会尽量保真回传

如果 worker 侧异常本身可序列化，swarm 会尽量回传原始 throwable 数据。  
如果该异常不可序列化，则会退化成一个 `RemoteException` 包装对象，保留远端异常类型、消息和堆栈，确保这些信息仍然可以安全传输。

### swarm 还有自己的扩展点

除了普通 Cucumber plugin 外，swarm 还会扫描：

- `MasterPluginExtension`
- `WorkerForwardingPluginExtension`

扫描包名为：

- `org.testcharm.cucumber.extensions`
- `org.testcharm.extensions.cucumber`

大多数使用者并不需要直接使用这些 API；但如果你要做的是“扩展 swarm 感知能力”，而不只是消费 master 侧聚合结果，它们就会有意义。
