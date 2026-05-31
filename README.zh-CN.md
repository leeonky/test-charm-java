# TestCharm for Java

[English](README.md)

[![CircleCI](https://dl.circleci.com/status-badge/img/gh/test-charm/test-charm-java/tree/main.svg?style=svg)](https://dl.circleci.com/status-badge/redirect/gh/test-charm/test-charm-java/tree/main)
[![codecov](https://codecov.io/gh/test-charm/test-charm-java/graph/badge.svg?token=R5F98WSH6F)](https://codecov.io/gh/test-charm/test-charm-java)
[![Mutation testing badge](https://img.shields.io/endpoint?style=flat&url=https%3A%2F%2Fbadge-api.stryker-mutator.io%2Fgithub.com%2Fleeonky%2Ftest-charm-java%2Fmain)](https://dashboard.stryker-mutator.io/reports/github.com/test-charm/test-charm-java/test-charm-java/main)
[![Last commit](https://img.shields.io/github/last-commit/test-charm/test-charm-java.svg)](https://github.com/test-charm/test-charm-java)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

[![jfactory](https://img.shields.io/maven-central/v/org.test-charm/jfactory.svg?label=jfactory)](https://search.maven.org/artifact/org.test-charm/jfactory)
[![DAL-java](https://img.shields.io/maven-central/v/org.test-charm/DAL-java.svg?label=DAL-java)](https://search.maven.org/artifact/org.test-charm/DAL-java)

TestCharm 是一组面向 Java 的测试工具库。它的核心是：**把一个业务场景真正需要的数据、规则和验证结果收敛到同一个测试文件里**，让测试既能表达业务，又能长期维护。

## 从一个具体需求开始

假设系统里有如下业务关系：

```text
Order
├─ trackingPackages
│ ├─ trackingNumber
│ ├─ status
│ ├─ warehouse
│ │ ├─ name
│ │ ├─ address
│ │ └─ code
│ └─ carrier
│   ├─ name
│   └─ code
├─ customer
│ ├─ name
│ ├─ phone
│ └─ address
├─ orderLines
│ ├─ product
│ │ ├─ name
│ │ ├─ price
│ │ └─ category
│ │   ├─ name
│ │   └─ code
│ └─ quantity
├─ orderId
├─ orderDate
├─ status
└─ createdAt
```

现在有一个导出订单快递信息的接口，直接返回 `.xlsx` 文件。需求本身并不复杂：准备一条带多层关联数据的订单，请求导出接口，然后验证导出的 Excel 表格内容。

困难恰恰出现在这里。对很多自动化测试方案来说，这类需求会立刻分裂成三件不同的事：

- 准备一组嵌套很深的测试数据
- 调用 HTTP 接口
- 解析一个非内存对象的 Excel 文件并逐格校验

下面用同一个需求，依次看几种常见写法，以及它们为什么仍然不够理想。

### 最直接的 Python 实现

最直接的实现方式，往往会落到一段完整的 fixture + API 调用 + Excel 解析代码上：

```python
import pytest
from datetime import datetime, timezone, timedelta
from io import BytesIO
import openpyxl

@pytest.fixture
def setup_order_data(db_session):
    from app.models import Order, Customer, TrackingPackage, Warehouse, Carrier

    customer = Customer(name="alice", phone="12345678901", address="1234 Sunset Blvd, Los Angeles, CA")
    db_session.add(customer)
    warehouse = Warehouse(name="LA Warehouse", address="800 Market St, San Francisco, CA", code="WH001")
    carrier = Carrier(name="FedEx", code="FDX")
    db_session.add(warehouse)
    db_session.add(carrier)
    db_session.flush()

    order = Order(
        order_id="ORD-001",
        order_date=datetime(2024, 1, 1, 0, 0, 0, tzinfo=timezone(timedelta(hours=8))),
        status="TO_BE_PICKED_UP",
        created_at=datetime(2024, 1, 1, 8, 0, 0, tzinfo=timezone(timedelta(hours=8))),
        customer_id=customer.id
    )
    db_session.add(order)
    db_session.flush()

    tp1 = TrackingPackage(
        tracking_number="TN-001",
        status="TO_BE_PICKED_UP",
        order_id=order.id,
        warehouse_id=warehouse.id,
        carrier_id=carrier.id
    )
    tp2 = TrackingPackage(
        tracking_number="TN-002",
        status="PICKED_UP",
        order_id=order.id,
        warehouse_id=warehouse.id,
        carrier_id=carrier.id
    )
    db_session.add(tp1)
    db_session.add(tp2)
    db_session.commit()

def test_export_tracking_packages(client, setup_order_data):
    response = client.get("/orders/export")
    assert response.status_code == 200

    excel_bytes = BytesIO(response.content)
    wb = openpyxl.load_workbook(excel_bytes)
    ws = wb["Orders"]
    rows = [[cell.value for cell in row] for row in ws.iter_rows()]

    assert rows[0] == [
        "Order ID", "Order Date", "Created At", "Status", "Tracking Number", "Package Status"
    ]
    assert rows[1] == [
        "ORD-001", "2024-01-01T00:00:00+08:00", "2024-01-01T08:00:00+08:00", "TO_BE_PICKED_UP", "TN-001", "TO_BE_PICKED_UP"
    ]
    assert rows[2] == [
        "ORD-001", "2024-01-01T00:00:00+08:00", "2024-01-01T08:00:00+08:00", "TO_BE_PICKED_UP", "TN-002", "PICKED_UP"
    ]
```

这种写法当然能工作，但测试数据准备、接口调用和断言逻辑完全揉在一起。随着场景增多，代码会很快膨胀，测试表达的重点也会被技术实现细节盖住。

### 数据驱动的接口测试

进一步的常见做法，是把数据和预期抽到 JSON、CSV 或 SQL 文件里，再配合参数化执行。形式上看，代码和数据分离了：

```json
[
  {
    "id": "basic_2pkgs",
    "data": {
      "customer": {
        "name": "alice",
        "phone": "12345678901",
        "address": "1234 Sunset Blvd, Los Angeles, CA"
      },
      "warehouse": {
        "name": "LA Warehouse",
        "address": "800 Market St, San Francisco, CA",
        "code": "WH001"
      },
      "carrier": {
        "name": "FedEx",
        "code": "FDX"
      },
      "order": {
        "order_id": "ORD-001",
        "order_date": {
          "$datetime": "2024-01-01T00:00:00+08:00"
        },
        "created_at": {
          "$datetime": "2024-01-01T08:00:00+08:00"
        },
        "status": "TO_BE_PICKED_UP"
      },
      "trackingPackages": [
        {
          "tracking_number": "TN-001",
          "status": "TO_BE_PICKED_UP"
        },
        {
          "tracking_number": "TN-002",
          "status": "PICKED_UP"
        }
      ]
    },
    "expected": [
      [
        "Order ID",
        "Order Date",
        "Created At",
        "Status",
        "Tracking Number",
        "Package Status"
      ],
      [
        "ORD-001",
        {
          "$datetime": "2024-01-01T00:00:00+08:00"
        },
        {
          "$datetime": "2024-01-01T08:00:00+08:00"
        },
        "TO_BE_PICKED_UP",
        "TN-001",
        "TO_BE_PICKED_UP"
      ],
      [
        "ORD-001",
        {
          "$datetime": "2024-01-01T00:00:00+08:00"
        },
        {
          "$datetime": "2024-01-01T08:00:00+08:00"
        },
        "TO_BE_PICKED_UP",
        "TN-002",
        "PICKED_UP"
      ]
    ]
  }
]
```

```python
@pytest.mark.parametrize("case_id,case_data,expected_rows", load_cases())
def test_order_export_case(client, db_session, setup_order, case_id, case_data, expected_rows):
    setup_order(case_data)
    response = client.get("/orders/export")
    assert response.status_code == 200

    wb = openpyxl.load_workbook(BytesIO(response.content))
    ws = wb["Orders"]
    rows = [[cell.value for cell in row] for row in ws.iter_rows()]

    for actual, expected in zip(rows, [parse_value(r) for r in expected_rows]):
        for a, e in zip(actual, expected):
            assert cell_equal(a, e)
```

这比第一种方式更规整，但问题并没有消失，只是换了位置：

- 数据文件会越来越像“为了数据库合法而填写的结构”，而不是场景真正关心的业务数据
- 业务规则仍然被拆成“数据文件 + 装载逻辑 + 断言代码”三段
- 用例更像接口回归样本，而不像一个可以直接阅读和讨论的业务场景

### 传统 BDD

再往前一步，团队通常会想到 BDD，希望测试不只是接口校验，而是直接描述业务场景：

```gherkin
Feature: Exporting tracking packages

  As a logistics operator
  I want to export order tracking information
  So that I can share package status with customers

  Scenario: Export an order with multiple tracking packages
    Given Alice places a new order ORD-001 on "2024-01-01T00:00:00+08:00"
    And the order is created at "2024-01-01T08:00:00+08:00"
    And the order contains two tracking packages:
      | Tracking Number | Package Status  |
      | TN-001          | TO_BE_PICKED_UP |
      | TN-002          | PICKED_UP       |
    And the status of the order is "TO_BE_PICKED_UP"
    When I export orders
    Then the Excel file "orders.xlsx" should be generated
    And the Orders sheet should contain the following rows:
      | Order ID | Order Date                | Created At                | Status          | Tracking Number | Package Status  |
      | ORD-001  | 2024-01-01T00:00:00+08:00 | 2024-01-01T08:00:00+08:00 | TO_BE_PICKED_UP | TN-001          | TO_BE_PICKED_UP |
      | ORD-001  | 2024-01-01T00:00:00+08:00 | 2024-01-01T08:00:00+08:00 | TO_BE_PICKED_UP | TN-002          | PICKED_UP       |
```

传统 BDD 显著提升了业务可读性，但它也有自己的代价：

- 场景更像自然语言说明，信息密度不一定高
- 精确数据和精确断言通常还是要靠额外 step 实现补齐
- step 数量容易不断增长，出现重复、语义漂移和维护成本上升

### TestCharm 的写法

自动化测试的本质并不复杂。所有的测试最终都在处理三件事：

1. 准备数据
2. 执行操作
3. 验证结果

这也是 BDD 的核心内涵。

TestCharm 希望保留 BDD 对业务场景的表达力，同时提高数据表达的密度和自动化实现效率。对于同样的需求，可以写成下面这样：

```gherkin
Feature: Tracking package export

  Scenario: export tracking packages
    Given exists data:
      """
      Order: {
        customer.name: alice
        orderId: ORD-001
        orderDate: '2024-01-01T00:00:00+08:00'
        status: TO_BE_PICKED_UP
        createdAt: '2024-01-01T08:00:00+08:00'
        trackingPackages: | trackingNumber | status          |
                          | TN-001         | TO_BE_PICKED_UP |
                          | TN-002         | PICKED_UP       |
      }
      """
    When GET "/orders/export"
    Then response should be:
      """
      : {
        code: 200
        fileName: 'orders.xlsx'
        body.excel[Orders]: | A        | B                        | C                           | D               | E               | F                  |
                            | Order ID | Order Date               | Created At                  | Status          | Tracking Number | Package Status     |
                            | ORD-001  | '2024-01-01T00:00+08:00' | '2024-01-01T08:00:00+08:00' | TO_BE_PICKED_UP | TN-001          | TO_BE_PICKED_UP    |
                            | ORD-001  | '2024-01-01T00:00+08:00' | '2024-01-01T08:00:00+08:00' | TO_BE_PICKED_UP | TN-002          | PICKED_UP          |
      }
      """
```

这种范式的核心，在于精准提取场景中最关键的业务要点和验证意图，通过简洁、准确且可复用的数据表达真正的业务决策点，并辅以必要的技术性提示词，帮助读者快速理解上下文，同时不影响业务表达本身的清晰度与专注度。

它并不刻意追求把测试用例写成传统 BDD 那种“纯业务自然语言”的故事剧本，也避免把测试退化成“接口验收工具”或“数据表填鸭式执行”那样偏冷冰冰的技术校验。它希望在业务可读性、自动化实现效率和长期维护能力之间取得更好的平衡，让测试真正成为需求和业务规则的表达与验证工具。

理想的测试用例，更像“读书笔记”，而不是一本写满文字的书：

- **有重点**：聚焦关键场景、主路径和断言结果，而不是铺陈过多背景与说明
- **有数据**：直接呈现实际业务规则、输入和输出，便于校验、回归和讨论
- **有简明的技术提示**：只承担导航和补充作用，不喧宾夺主

由此带来的好处也很直接：

- **简单**：不依赖额外工具或 IDE，一个文本文件就能承载完整的业务说明
- **可 diff**：用例变更直接体现在 git diff 里，数据改了什么、规则加了什么，code review 一目了然
- **AI 友好**：大语言模型可以直接读取 feature 文件理解业务，不需要先运行代码或加载数据库
- **自包含**：每个场景只携带自己需要的数据，场景之间互不干扰，失败时也更容易看出“因为什么数据，导致什么结果不符合预期”

## TestCharm

基于以上的目标，TestCharm 库应运而生。其核心设计在于两点：

- 允许开发者仅关注和定义复杂测试数据中的关键最小子集，框架自动补全其它所需的默认和合法性数据；
- 保障测试数据及断言能够以简洁明了、精准的方式进行表达与展现。

同时，测试编写者借由这个框架，无需编写步骤实现（step definition）代码，即可高效、清晰地完成复杂场景的数据准备与验证。

TestCharm 的核心由两个模块组成。

### JFactory

[jfactory](jfactory/README.zh-CN.md) 是测试数据准备库。它通过 Spec、Trait、级联创建和 repository 复用等机制，让测试只写当前场景真正关心的数据，而把默认值、合法性和对象关系交给框架处理。

如果想进一步了解 JFactory 的使用方式、Repository 复用能力以及在 Cucumber 中如何准备数据，建议继续阅读：

- [jfactory](jfactory/README.zh-CN.md)
- [jfactory-cucumber](jfactory-cucumber/README.zh-CN.md)
- [jfactory-repo-jpa](jfactory-repo-jpa/README.zh-CN.md)

### DAL-java

[DAL-java](DAL-java/README.zh-CN.md) 是数据描述和断言引擎。它提供一套接近 JSON、但更适合测试表达的数据语言，用来统一描述对象、列表、表格、文件、HTTP 响应、数据库结果和 UI 数据。

围绕 DAL 的常见扩展包括：

- [DAL-extension-basic](DAL-extension-basic/README.zh-CN.md)：文件、ZIP、异步断言等常用扩展
- [DAL-extension-jdbc](DAL-extension-jdbc/README.zh-CN.md)：直接访问和断言数据库表数据
- [DAL-extension-jfactory](DAL-extension-jfactory/README.zh-CN.md)：从 DAL 直接查询 JFactory repository 数据

## 相关模块

除了 JFactory 和 DAL-java，仓库还包含一组面向常见测试场景的配套模块：

| 模块                                                   | 作用                                      |
|------------------------------------------------------|-----------------------------------------|
| [RESTful-cucumber](RESTful-cucumber/README.zh-CN.md) | 复用 HTTP 请求与响应验证步骤，把 REST API 测试接到 DAL 上 |
| [page-flow](page-flow/README.zh-CN.md)               | 将 UI 定位、操作和断言组织成结构化数据表达                 |
| [bean-util](bean-util/README.zh-CN.md)               | 供核心库复用的底层 bean 与反射工具                    |

如果从使用顺序来看，一个更自然的阅读路径通常是：

1. [DAL-java](DAL-java/README.zh-CN.md)
2. [jfactory](jfactory/README.zh-CN.md)
3. [RESTful-cucumber](RESTful-cucumber/README.zh-CN.md) 或 [page-flow](page-flow/README.zh-CN.md)
4. 再按需要进入数据库相关扩展

## TestCharm 与 Cucumber 的关系

TestCharm 经常与 Cucumber 搭配使用，因为 Gherkin 很适合承载场景化、数据化的测试表达。

但 TestCharm 的核心——JFactory 和 DAL——本身都是独立的 Java 库，并不依赖 Cucumber 才能使用。Cucumber 相关模块更适合作为接入层和复用层，而不是唯一使用方式。

## 运行项目测试

仓库里既有纯单元测试，也有依赖额外环境的测试。若要在本地尽量复现 CI 的执行环境，建议先准备以下条件。

### 环境要求

- 本地安装 JDK 17
- 安装 Docker 与 Docker Compose
- 可以修改 `/etc/hosts`
- 本地存在可写的 `/tmp/testcharm` 目录

虽然发布出去的库以 Java 8 为目标版本，但仓库测试在 CI 中使用 JDK 17，并额外执行 Java 17 的模块安全检查。

### 测试依赖的服务

`docker-compose.yml` 会启动与 CI 相同的辅助服务：

- `2222` 端口上的 SSH 服务
- `4444` 端口上的 Selenium Chromium 服务
- `3000` 端口上的 Playwright 服务

启动命令：

```bash
docker-compose up -d
```

### hosts 配置

CI 在运行测试前会加入以下 hosts 映射：

```bash
echo '127.0.0.1 www.s.com' | sudo tee -a /etc/hosts
echo '127.0.0.1 www.a.com' | sudo tee -a /etc/hosts
```

其中 Java 17 模块安全相关任务至少需要 `www.a.com`；完整构建任务还会加入 `www.s.com`。

### 接近 CI 的完整运行方式

```bash
mkdir -p /tmp/testcharm
chmod 777 /tmp/testcharm
export CI=true
export DAL_INSPECTOR_WAITING_TIME=30000
./gradlew test build --parallel
```

### 常用命令

运行完整构建：

```bash
./gradlew build
```

按模块运行测试：

```bash
./gradlew :DAL-java:test
./gradlew :jfactory:test
./gradlew :RESTful-cucumber:test
```

运行某个模块的 Cucumber 场景：

```bash
./gradlew :DAL-java:cucumber
./gradlew :jfactory:cucumber
```

运行 Java 17 模块安全检查：

```bash
echo '127.0.0.1 www.a.com' | sudo tee -a /etc/hosts
./gradlew testModuleSafe
./gradlew testRequiresAddOpens
```

## 联系方式

如果你希望进一步了解某个模块或能力，最直接的资料仍然是仓库中的模块文档和示例测试，也可以直接联系作者：

- `leeonky@gmail.com`
- `joseph.yao.ruozhou@gmail.com`

## License

[Apache License 2.0](LICENSE)
