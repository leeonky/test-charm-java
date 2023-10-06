Feature: assert has many

  Scenario: has many with where clause
    Given Exists data "OrderLine":
      | quantity | order.code    | product.name |
      | 1        | S01           | MBP          |
      | 2        | S01           | iPod         |
      | 3        | another_order | any          |
    When all follow tables:
      | products | order_lines | orders |
    Then db should:
      """
      orders[0]: {
        code= S01
        ::hasMany[order_lines]::on[order_id = :id]: | quantity | ::belongsTo[products].name |
                                         | 1        | MBP                        |
                                         | 2        | iPod                       |

        ::hasMany[order_lines]::on[order_id]: | quantity | ::belongsTo[products].name |
                                         | 1        | MBP                        |
                                         | 2        | iPod                       |

        ::hasMany[order_lines]::on[:id]: | quantity | ::belongsTo[products].name |
                                         | 1        | MBP                        |
                                         | 2        | iPod                       |

        ::hasMany[order_lines]: | quantity | ::belongsTo[products].name |
                                | 1        | MBP                        |
                                | 2        | iPod                       |
      }
      """

  Scenario: has many return empty list
    Given Exists data "Order":
      | code |
      | S01  |
    When all follow tables:
      | products | order_lines | orders |
    Then db should:
      """
      orders: [{
        code= S01
        ::hasMany[order_lines]: []
      }]
      """

  Scenario: override default join column
    Given Exists data "Product":
      | id | name     | pid |
      | 1  | product1 | 2   |
      | 2  | product2 | 1   |
    Given Exists data "OrderLine":
      | product.name | quantity |
      | product1     | 1        |
      | product2     | 2        |
#    unexpected
    Given Exists data "OrderLine":
      | product |
      |         |
    When all follow tables:
      | products | order_lines |
    Then db should:
      """
      products: [{
        name= product1
        ::hasMany[order_lines]: | quantity | ::belongsTo[products].name |
                                | 1        | product1                   |

        ::hasMany[order_lines]::on[:pid]: | quantity | ::belongsTo[products].name |
                                          | 2        | product2                   |
      } {
        name= product2
        ::hasMany[order_lines]: | quantity | ::belongsTo[products].name |
                                | 2        | product2                   |

        ::hasMany[order_lines]::on[:pid]: | quantity | ::belongsTo[products].name |
                                          | 1        | product1                   |
      }]
      """

  Scenario: override default reference column
    Given Exists data "Product":
      | id | name     |
      | 1  | product1 |
      | 2  | product2 |
    Given Exists data "OrderLine":
      | product.name | quantity | refId |
      | product1     | 1        | 2     |
      | product2     | 2        | 1     |
#    unexpected
    Given Exists data "OrderLine":
      | product |
      |         |
    When all follow tables:
      | products | order_lines |
    Then db should:
      """
      products: [{
        name= product1
        ::hasMany[order_lines]: | quantity | ::belongsTo[products].name |
                                | 1        | product1                   |

        ::hasMany[order_lines]::on[refid]: | quantity | ::belongsTo[products].name |
                                           | 2        | product2                   |
      } {
        name= product2
        ::hasMany[order_lines]: | quantity | ::belongsTo[products].name |
                                | 2        | product2                   |

        ::hasMany[order_lines]::on[refid]: | quantity | ::belongsTo[products].name |
                                           | 1        | product1                   |
      }]
      """

  Scenario: raise error when invalid sql
    Given Exists 1 data "Order"
    When assert DB:
      """
      orders: [{
        ::hasMany[order_lines]::on[:id=not_exist]: []
      }]
      """
    Then raise error
    """
    message.trim: ```
                  orders: [{
                    ::hasMany[order_lines]::on[:id=not_exist]: []
                                              ^
                  }]

                  org.h2.jdbc.JdbcSQLSyntaxErrorException: Column "NOT_EXIST" not found; SQL statement:
                  select order_lines.* from order_lines where ?=not_exist [42122-200]

                  The root value was: com.github.leeonky.dal.extensions.jdbc.DataBase {
                      orders: [
                          com.github.leeonky.dal.extensions.jdbc.DataBase$Row {
                              id: java.lang.Long <4>,
                              code: java.lang.String <code#4>
                          }
                      ]
                  }
                  ```
    """
