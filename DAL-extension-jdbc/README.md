DAL Extension of DataBase use JDBC:

[![CircleCI](https://dl.circleci.com/status-badge/img/gh/leeonky/DAL-extension-jdbc/tree/main.svg?style=svg)](https://dl.circleci.com/status-badge/redirect/gh/leeonky/DAL-extension-jdbc/tree/main)
[![coveralls](https://img.shields.io/coveralls/github/leeonky/DAL-extension-jdbc.svg)](https://coveralls.io/github/leeonky/DAL-extension-jdbc)
[![Lost commit](https://img.shields.io/github/last-commit/leeonky/DAL-extension-jdbc.svg)](https://github.com/leeonky/DAL-extension-jdbc)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.leeonky/DAL-extension-jdbc.svg)](https://search.maven.org/artifact/com.github.leeonky/DAL-extension-jdbc)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/fdb168b942fd4b49abc83fed301a046d)](https://app.codacy.com/project/leeonky/DAL-extension-jdbc/dashboard)
[![Maintainability](https://api.codeclimate.com/v1/badges/03019efcb353a9399463/maintainability)](https://codeclimate.com/github/leeonky/DAL-extension-jdbc/maintainability)
[![Code Climate issues](https://img.shields.io/codeclimate/issues/leeonky/DAL-extension-jdbc.svg)](https://codeclimate.com/github/leeonky/DAL-extension-jdbc/maintainability)
[![Code Climate maintainability (percentage)](https://img.shields.io/codeclimate/maintainability-percentage/leeonky/DAL-extension-jdbc.svg)](https://codeclimate.com/github/leeonky/DAL-extension-jdbc/maintainability)

Given the following data table in database H2

orders:

id |  customer
---|---
S01 |Tom

products:

id | name
---|--------
p1 | iPhone |
p2 | MBP    |

order_lines:

id | product_id | order_id | quantity |
---|---|---|---
1 |p1 |S01| 1
2 |p2 |S01| 100

Then you can assert database through DAL:

```
    Connection connection=DriverManager.getConnection("jdbc:h2:mem:test","sa","");

    expect(new DataBaseBuilder().connect(connection)).should("""
        : {
            products: | id | name   |
                      | p1 | iPhone |
                      | p2 | MBP    |
                      
            orders: [{
                customer= Tom
                ::hasMany[order_lines]: | id | product_id | order_id | quantity | ::belongsTo[products].name |
                                        | 1  | p1         | S01      | 1        | iPhone                     |
                                        | 2  | p2         | S01      | 100      | MBP                        |
            }]
        }
    """);
```

You can also define some 'method' on table row

```
    DataBaseBuilder builder = new DataBaseBuilder();
    builder.tableStrategy("orders").registerRowMethod("orderLines", row ->
            row.hasMany("order_lines"));
            
    builder.tableStrategy("order_lines").registerRowMethod("product", row ->
            row.belongsTo("products"));
```

And then you can query association data directly

```
    expect(builder.connect(connection)).should("""
        : {
            orders: [{
                customer= Tom
                orderLines: | id | quantity | products.name |
                            | 1  | 1        | iPhone        |
                            | 2  | 100      | MBP           |
            }]
        }
    """);
```

You can see more examples by test case
in [here](https://github.com/leeonky/DAL-extension-jdbc/tree/main/src/test/resources/features)