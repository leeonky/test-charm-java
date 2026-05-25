# cucumber-swarm

`cucumber-swarm` 在标准 Cucumber CLI 之上增加了一层 **master / worker 协调器**。  
master 负责解析 feature、生成待执行场景队列、启动 HTTP 协调服务；worker 负责真正执行场景，并把执行过程中的事件回传给 master，再由 master 转发到本地 `EventBus`，所以最终的插件、summary 和退出码仍然从 master 侧产出。

本文档依据以下来源整理：

- `src/test/resources/features/` 下全部 BDD 测试
- `src/test/java/` 下参数与序列化测试
- `src/main/java/` 下实现代码

## 1. 与标准 Cucumber 相比新增的参数

这些参数由 `org.testcharm.cucumber.swarm.WorkerArgsPreProcessor` 预处理。

| 参数 | 默认值 | 作用 |
| --- | --- | --- |
| `--swarm-port <port>` | `10083` | master 内置 REST 服务端口，worker 通过它领取场景并回传事件。 |
| `--local-worker enable\|disable` | `enable` | 是否在 master 进程内再启动一个本地 worker。 |
| `--remote-worker-count <n>` | `0` | 让 master 额外拉起多少个远程 worker 进程。 |
| `--worker-timeout <seconds>` | `5` | master 等待首个 worker ready 的超时时间。超时直接失败。 |
| `--working-dir <path>` | 当前 `user.dir` | master / worker 做路径映射时使用的工作目录。决定 pickle key、test case key 等相对路径长什么样。 |
| `--worker-id <id>` | 无 | 标识当前进程是 worker 侧进程。这个参数通常由 master 注入，不是给普通使用者单独跑 master 时用的。 |

### 额外约定

1. `--threads` 会被强制改成 `1`，无论是否显式传入。master 和 worker 都是单线程执行。
2. `--plugin` 只会保留在 master 侧；worker 侧会改成固定插件 `org.testcharm.cucumber.swarm.WorkerForwardingPlugin`。
3. `--no-summary` 对 master 侧生效；本地 worker 默认总是 `--no-summary`，避免重复输出 summary。
4. 在命令中使用 `--` 之后的内容，不再当作 Cucumber 参数，而是当作 **远程 worker 启动命令模板**。
5. 远程 worker 启动参数模板里支持占位符 `{worker-id}`，master 启动每个远程 worker 时会替换成实际 id。

## 2. 最常见的启动方式

### 2.1 本地单 worker 模式

```bash
java ... org.testcharm.cucumber.swarm.Main \
  --glue steps \
  features
```

效果：

- master 启动
- 自动再起一个本地 worker
- 本地 worker 从 master 领场景执行
- 所有 summary 和插件输出仍由 master 统一产出

### 2.2 远程 worker 模式

```bash
java ... org.testcharm.cucumber.swarm.Main \
  --local-worker disable \
  --remote-worker-count 2 \
  --glue steps \
  features \
  -- \
  java -cp app.jar org.testcharm.cucumber.swarm.Main \
    --swarm-port 10083 \
    --worker-id {worker-id} \
    --glue steps \
    features
```

这里 `--` 后面的命令是远程 worker 模板。master 会把 `{worker-id}` 替换成 `1`、`2` 等实际值后分别启动。

## 3. 基本架构

```text
                +--------------------+
                |       Master       |
                |--------------------|
                | FeatureSupplier    |
                | Pickle queue       |
                | MasterPlugin       |
                | MasterDataMapper   |
                | RestfulServer      |
                +----+----------+----+
                     |          |
          GET /pickle|          |POST /events, /ready
                     |          |
          +----------v--+   +---v-----------+
          |   Worker 1  |   |   Worker N    |
          |-------------|   |---------------|
          | WorkerRuntime|  | WorkerRuntime |
          | ForwardPlugin|  | ForwardPlugin |
          | WorkerDataMap|  | WorkerDataMap |
          +-------------+   +---------------+
```

### 3.1 master 做什么

- 解析 feature，筛选并排序 pickle
- 用 `TestCaseFactory` 预先把 pickle 映射成 `TestCase`
- 启动 `RestfulServer`
- 启动本地 worker 和/或远程 worker
- 维护一个待执行 `pickleQueue`
- 接收 worker 回传事件
- 把回传事件反序列化后重新发到 master 的 `EventBus`

关键类：

- `Main`
- `io.cucumber.core.runtime.MasterRuntime`
- `io.cucumber.core.runtime.MasterCucumberExecutionContext`
- `org.testcharm.cucumber.swarm.master.Master`
- `org.testcharm.cucumber.swarm.master.Controller`
- `org.testcharm.cucumber.swarm.master.RestfulServer`

### 3.2 worker 做什么

- 启动后先向 master 发送 ready 信号
- 通过 `/pickle` 逐个领取待执行场景
- 在本地运行 Cucumber 场景
- 通过 `WorkerForwardingPlugin` 把需要的事件序列化并回传给 master

关键类：

- `io.cucumber.core.runtime.WorkerRuntime`
- `org.testcharm.cucumber.swarm.worker.Remote`
- `org.testcharm.cucumber.swarm.worker.RestfulClient`
- `org.testcharm.cucumber.swarm.WorkerForwardingPlugin`
- `org.testcharm.cucumber.swarm.worker.EventSerializer`

### 3.3 路径映射为什么重要

master 和 worker 都通过 `DataMapper` 把对象映射成相对路径 key，例如：

- `features/test.feature:3`

这个 key 被用于：

- pickle 分发
- `TestCase` 对齐
- step definition / hook / test step 回填
- 跨进程把 worker 侧路径重新映射回 master 侧路径

`--working-dir` 的意义就是保证两边生成的 key 一致。

## 4. master 与 worker 之间的 REST 协议

`Controller` 只暴露 3 个接口：

| 方法 | 路径 | 请求头 | 请求体 | 返回 |
| --- | --- | --- | --- | --- |
| `GET` | `/pickle` | `X-Worker-Id` | 无 | 200 时返回一个 pickle key；没有更多场景时返回 404。 |
| `POST` | `/events` | `X-Worker-Id` | 一个 JSON 事件记录 | 200 表示 master 已接收并转发。 |
| `POST` | `/ready` | `X-Worker-Id` | 空串 | 200 表示 worker 已标记为 ready。 |

### 启动和关闭时序

典型日志顺序如下：

1. master 创建并记录场景数
2. REST 服务启动
3. worker 启动
4. worker 发送 ready
5. worker 循环请求 `/pickle`
6. master 队列清空后关闭
7. master 等待所有 worker 退出
8. REST 服务关闭

当没有 worker 在 `workerTimeout` 内 ready 时，master 会直接抛出：

```text
No worker available after waiting for <n> seconds
```

## 5. worker 会回传哪些消息

分两层理解：

### 5.1 协调消息

这类消息不是 Cucumber 事件，而是 master / worker 协议本身：

- `ready`：worker 已可接活
- `pickle request`：worker 请求下一个场景
- `pickle key`：master 返回一个待执行场景标识
- `no pickle`：没有更多场景，worker 结束

### 5.2 回传到 master EventBus 的 Cucumber 事件

worker 只转发下面 4 类 `io.cucumber.plugin.event` 事件：

- `TestCaseStarted`
- `TestStepStarted`
- `TestStepFinished`
- `TestCaseFinished`

其他事件不会转发，日志中会看到 `ignore event forwarding`，测试覆盖了这些典型例子：

- `TestRunStarted`
- `TestSourceRead`
- `TestSourceParsed`
- `StepDefinedEvent`
- `SnippetsSuggestedEvent`
- `TestRunFinished`

### 5.3 同时回传的 message 层对象

除了上面的 plugin event，worker 还会把对应的 `io.cucumber.messages.types` 对象一并回传，当前覆盖到：

- `TestCase`
- `TestCaseStarted`
- `TestStepStarted`
- `TestStepFinished`
- `TestCaseFinished`

这部分由 `Envelope` 事件触发，master 收到后会反序列化成 `Envelope.of(...)` 再送回本地总线。

## 6. 各类回传消息包含什么字段

### 6.1 `io.cucumber.plugin.event.TestCaseStarted`

master 侧最终拿到的是完整 `TestCase` 对象，测试证明会保留：

- `instant`
- `testCase.location`
- `testCase.uri`
- `testCase.testSteps`

`testSteps` 里可能出现两类步骤：

- `PickleStepTestStep`
- `HookTestStep`

### 6.2 `io.cucumber.plugin.event.TestStepStarted`

会带回：

- `testCase`
- `testStep`

并且 `testStep` 能和 `testCase.testSteps[index]` 对应上。

### 6.3 `io.cucumber.plugin.event.TestStepFinished`

会带回：

- `testCase`
- `testStep`
- `result.status`
- `result.duration`
- `result.error`

测试覆盖的状态包括：

- `PASSED`
- `FAILED`
- `SKIPPED`
- `PENDING`
- `UNDEFINED`
- `AMBIGUOUS`

异常会被序列化后再恢复：

- 普通异常保留类型、message、stack trace
- 无法原样反序列化的异常会退化成 `RemoteException`

### 6.4 `io.cucumber.plugin.event.TestCaseFinished`

会带回：

- `instant`
- `testCase`
- `result.status`
- `result.duration`
- `result.error`

### 6.5 `io.cucumber.messages.types.TestCase`

会带回结构化 message 版本的测试用例定义，重点字段包括：

- `id`
- `pickleId`
- `testSteps`
- `hookId`
- `pickleStepId`
- `stepDefinitionIds`
- `stepMatchArgumentsLists`

这使 master 侧既能恢复普通步骤，也能恢复 hook 步骤，还能保留正则/参数化匹配信息。

### 6.6 `io.cucumber.messages.types.TestCaseStarted`

会带回：

- `attempt`
- `id`
- `testCaseId`
- `workerId`
- `timestamp`

### 6.7 `io.cucumber.messages.types.TestStepStarted`

会带回：

- `testCaseStartedId`
- `testStepId`
- `timestamp`

### 6.8 `io.cucumber.messages.types.TestStepFinished`

会带回：

- `testCaseStartedId`
- `testStepId`
- `timestamp`
- `result.duration`
- `result.message`
- `result.status`
- `result.exception`

### 6.9 `io.cucumber.messages.types.TestCaseFinished`

会带回：

- `testCaseStartedId`
- `timestamp`
- `willBeRetried`

## 7. 参数和插件行为上的几个容易忽略的点

### 7.1 master 与 worker 的插件分工

- master 固定注入 `MasterPlugin`
- worker 固定注入 `WorkerForwardingPlugin`
- 用户自己传的 `--plugin` 只加在 master

这样可以避免 worker 直接打印 summary 或重复消费事件。

### 7.2 本地 worker 与远程 worker 的 summary 行为不同

- 本地 worker 默认强制 `--no-summary`
- 远程 worker 进程因为是显式 `--worker-id` 模式，预处理后不会保留那条强制禁用逻辑，所以最终仍由实际 worker 参数决定

### 7.3 指定 feature 文件路径是支持的

测试覆盖了直接执行单个 feature 文件：

```bash
... features/test.feature
```

不是必须传目录。

## 8. 已被测试验证的行为

- 空 feature 也能完整走完 master / worker 生命周期
- undefined step 会返回 snippet，并让总体退出码为 `1`
- passed / failed / skipped / pending / ambiguous 都能正确汇总到 master
- hook step 与普通 pickle step 能一起正确回传
- 参数化 step 的匹配参数和嵌套 group 能完整回传
- worker 超时未就绪时会直接失败
- 本地 worker 和远程 worker 两种模式都已覆盖

## 9. 读代码时最值得先看的文件

如果要继续维护这个模块，建议按这个顺序看：

1. `org.testcharm.cucumber.swarm.WorkerArgsPreProcessor`
2. `org.testcharm.cucumber.swarm.Main`
3. `io.cucumber.core.runtime.MasterRuntime`
4. `io.cucumber.core.runtime.WorkerRuntime`
5. `org.testcharm.cucumber.swarm.master.Master`
6. `org.testcharm.cucumber.swarm.master.Controller`
7. `org.testcharm.cucumber.swarm.WorkerForwardingPlugin`
8. `org.testcharm.cucumber.swarm.worker.EventSerializer`
9. `org.testcharm.cucumber.swarm.master.EventDeserializer`
