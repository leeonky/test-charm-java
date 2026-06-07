# TestCharm for Java

[简体中文](README.zh-CN.md)

[![CircleCI](https://dl.circleci.com/status-badge/img/gh/test-charm/test-charm-java/tree/main.svg?style=svg)](https://dl.circleci.com/status-badge/redirect/gh/test-charm/test-charm-java/tree/main)
[![codecov](https://codecov.io/gh/test-charm/test-charm-java/graph/badge.svg?token=R5F98WSH6F)](https://codecov.io/gh/test-charm/test-charm-java)
[![Mutation testing badge](https://img.shields.io/endpoint?style=flat&url=https%3A%2F%2Fbadge-api.stryker-mutator.io%2Fgithub.com%2Fleeonky%2Ftest-charm-java%2Fmain)](https://dashboard.stryker-mutator.io/reports/github.com/test-charm/test-charm-java/test-charm-java/main)
[![Last commit](https://img.shields.io/github/last-commit/test-charm/test-charm-java.svg)](https://github.com/test-charm/test-charm-java)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

[![jfactory](https://img.shields.io/maven-central/v/org.test-charm/jfactory.svg?label=jfactory)](https://search.maven.org/artifact/org.test-charm/jfactory)
[![DAL-java](https://img.shields.io/maven-central/v/org.test-charm/DAL-java.svg?label=DAL-java)](https://search.maven.org/artifact/org.test-charm/DAL-java)

TestCharm is a set of testing libraries for Java. Its core idea is simple: **bring the data, rules, and expected results that truly matter to a business scenario into the same test file**, so tests can stay expressive and maintainable at the same time.

## Start with a concrete requirement

Assume the system has the following business relationships:

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

Now imagine an API that exports order shipment information directly as an `.xlsx` file. The requirement itself is not complicated: prepare one order with nested related data, call the export endpoint, then verify the Excel table content.

The difficulty starts exactly there. For many automation approaches, this kind of requirement immediately splits into three different problems:

- prepare a deeply nested set of test data
- call an HTTP API
- parse an Excel file, which is not an in-memory object, and verify cells one by one

Using the same requirement, the following sections walk through several common styles and why they are still not ideal.

### The most direct Python implementation

The most direct implementation often turns into one full block of fixture setup, API calls, and Excel parsing code:

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

This works, of course, but test data setup, API invocation, and assertion logic are all mixed together. As scenarios grow, the code expands quickly, and the actual point of the test gets buried under implementation detail.

### Data-driven API testing

The next common step is to move data and expectations into JSON, CSV, or SQL files and run them through parameterization. On the surface, this separates code from data:

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

This is more organized than the first style, but the problem has only moved:

- data files gradually become structures filled in for database validity rather than for the business facts the scenario actually cares about
- the business rule is still split across three places: data file, loading logic, and assertion code
- the result feels more like an API regression sample than a scenario people can read and discuss directly

### Traditional BDD

One step further, teams usually turn to BDD so that tests describe business scenarios instead of only checking interfaces:

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

Traditional BDD improves business readability significantly, but it has its own costs:

- scenarios can become more like natural-language descriptions than dense specifications
- exact data and exact assertions still usually need extra step implementations underneath
- the number of steps tends to grow continuously, bringing repetition, semantic drift, and maintenance overhead

### The TestCharm style

The nature of automated testing is not especially complicated. In the end, every test is dealing with three things:

1. prepare data
2. execute an action
3. verify the result

This is also the core idea behind BDD.

TestCharm aims to keep BDD's ability to express business scenarios while increasing the density of data expression and the efficiency of automation. For the same requirement, the scenario can be written like this:

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

The core of this style is to extract the most important business points and verification intent from the scenario precisely, then express the real decision points through concise, accurate, and reusable data. Necessary technical cue words are still present, but only to provide context; they do not distract from the clarity or focus of the business expression itself.

It does not deliberately try to turn test cases into the kind of "pure business natural language" story script often associated with traditional BDD, and it also avoids reducing tests to something as cold as an "interface acceptance tool" or a "data-table feeding mechanism." The goal is to strike a better balance among business readability, automation efficiency, and long-term maintainability, so that tests can truly serve as a way to express and verify requirements and business rules.

An ideal test case is more like reading notes than a book filled wall-to-wall with text:

- **focused**: it stays centered on the key scenario, main path, and expected result instead of surrounding them with too much setup narrative
- **data-rich**: it shows the actual business rules, inputs, and outputs directly, making validation, regression, and discussion easier
- **lightly technical where needed**: technical hints are there for navigation and support, but they do not dominate the document

The benefits are practical:

- **simple**: a plain text file can carry the full business explanation without depending on an external tool or IDE
- **diff-friendly**: changes show up directly in git diff, making it easy to review what data changed and what rule was added
- **AI-friendly**: large language models can read feature files directly to understand the business without first running code or loading a database
- **self-contained**: each scenario carries only the data it needs, so scenarios do not interfere with one another, and failures make it easier to see which data led to which unexpected result

## TestCharm

Against that background, TestCharm emerged with two core design goals:

- allow developers to focus only on the smallest critical subset of complex test data, while the framework fills in the default and validity data required by the rest of the object graph
- ensure that test data and assertions can be expressed in a concise, precise, and readable form

At the same time, the framework allows test authors to prepare and verify data for complex scenarios efficiently and clearly without having to write step definition code.

TestCharm is centered on two core modules.

### JFactory

[jfactory](jfactory/README.md) is the test data preparation library. Through Specs, Traits, cascading creation, and repository reuse, it lets tests define only the data that matters to the current scenario while leaving defaults, validity requirements, and object relationships to the framework.

If you want to go deeper into JFactory usage, repository reuse, and how data setup works in Cucumber, the best next documents are:

- [jfactory](jfactory/README.md)
- [jfactory-cucumber](jfactory-cucumber/README.md)
- [jfactory-repo-jpa](jfactory-repo-jpa/README.md)

### DAL-java

[DAL-java](DAL-java/README.md) is the data description and assertion engine. It provides a language that is close to JSON, but better suited to testing, and uses that language to describe objects, lists, tables, files, HTTP responses, database results, and UI data in a consistent way.

Common extensions around DAL include:

- [DAL-extension-basic](DAL-extension-basic/README.md): common extensions for files, ZIP archives, asynchronous assertions, and related tasks
- [DAL-extension-jdbc](DAL-extension-jdbc/README.md): direct access to and assertions against relational table data
- [DAL-extension-jfactory](DAL-extension-jfactory/README.md): query JFactory repository data directly from DAL

## Related modules

In addition to JFactory and DAL-java, the repository includes a set of supporting modules for common testing scenarios:

| Module | Purpose |
| --- | --- |
| [RESTful-cucumber](RESTful-cucumber/README.md) | Reuse HTTP request and response verification steps, connecting REST API tests to DAL |
| [page-flow](page-flow/README.md) | Organize UI locating, interaction, and assertions as structured data expressions |
| [bean-util](bean-util/README.md) | Low-level bean and reflection utilities reused by the core libraries |

If you want a practical reading order, a natural path is usually:

1. [DAL-java](DAL-java/README.md)
2. [jfactory](jfactory/README.md)
3. [RESTful-cucumber](RESTful-cucumber/README.md) or [page-flow](page-flow/README.md)
4. Then move to the database-related extensions as needed

## TestCharm and Cucumber

TestCharm is often used together with Cucumber because Gherkin is a good fit for scenario-based, data-centered test expression.

However, the TestCharm core—JFactory and DAL—consists of independent Java libraries and does not depend on Cucumber to be useful. The Cucumber-related modules are better understood as integration and reuse layers, not the only way to use the project.

## Running the project tests

The repository contains both plain unit tests and tests that rely on extra environment setup. To reproduce CI locally as closely as practical, prepare the following first.

### Requirements

- JDK 17 installed locally
- Docker and Docker Compose
- permission to edit `/etc/hosts`
- a writable `/tmp/testcharm` directory

Although the published libraries target Java 8, the repository tests run on JDK 17 in CI, and CI also executes Java 17 module-safety checks.

### Services required by the test suite

`docker-compose.yml` starts the same helper services used in CI:

- an SSH service on port `2222`
- a Selenium Chromium service on port `4444`
- a Playwright service on port `3000`

Start them with:

```bash
docker-compose up -d
```

### hosts configuration

CI adds the following host mappings before running tests:

```bash
echo '127.0.0.1 www.s.com' | sudo tee -a /etc/hosts
echo '127.0.0.1 www.a.com' | sudo tee -a /etc/hosts
```

The Java 17 module-safety tasks require at least `www.a.com`; the full build job also adds `www.s.com`.

### A CI-like local run

```bash
mkdir -p /tmp/testcharm
chmod 777 /tmp/testcharm
export CI=true
export DAL_INSPECTOR_WAITING_TIME=30000
./gradlew test build --parallel
```

### Common commands

Run the full build:

```bash
./gradlew build
```

Run tests for a specific module:

```bash
./gradlew :DAL-java:test
./gradlew :jfactory:test
./gradlew :RESTful-cucumber:test
```

Run Cucumber scenarios for a module:

```bash
./gradlew :DAL-java:cucumber
./gradlew :jfactory:cucumber
```

Run the Java 17 module-safety checks:

```bash
echo '127.0.0.1 www.a.com' | sudo tee -a /etc/hosts
./gradlew testModuleSafe
./gradlew testRequiresAddOpens
```

## Contact

If you want to learn more about a module or capability, the most direct references are still the module documentation and example tests in the repository. You can also contact the authors directly:

- `leeonky@gmail.com`
- `joseph.yao.ruozhou@gmail.com`

## Third-party notices

This repository includes third-party software and source code adaptations. Copyright and license texts are retained in
the corresponding module artifacts and source tree:

| Component | Type | License | Compliance files |
| --- | --- | --- | --- |
| `bean-util` | `org.json` (JSON-java) shaded and relocated to `org.testcharm.shaded.org.json` | JSON License | `bean-util/src/main/resources/META-INF/NOTICE`, `bean-util/src/main/resources/META-INF/LICENSE-THIRD-PARTY` |
| `cucumber-swarm` | Source code adapted from `io.cucumber:cucumber-core` (`io/cucumber/core/**`) | Apache License 2.0 | `cucumber-swarm/src/main/resources/META-INF/NOTICE`, `cucumber-swarm/src/main/resources/META-INF/LICENSE-THIRD-PARTY` |

For a repository-level summary, see [NOTICE](NOTICE).

## License

[Apache License 2.0](LICENSE)
