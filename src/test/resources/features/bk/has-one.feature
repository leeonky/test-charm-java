Feature: assert has one

  Scenario: has one with where clause
    Given Exists data "OrderLine":
      | quantity | order.code | product.name |
      | 1        | S01        | MBP          |
#    unexpected
    Given Exists data "OrderLine":
      | order |
      |       |
    When all follow tables:
      | products | order_lines | orders |
    Then db should:
      """
      orders: [{
        code= S01
        ::hasOne[order_lines]::where[order_id = :id]: {
          quantity: 1,
          ::belongsTo[products].name= MBP
        }

        ::hasOne[order_lines]::where[order_id]: {
          quantity: 1,
          ::belongsTo[products].name= MBP
        }

        ::hasOne[order_lines]::on[:id]: {
          quantity: 1,
          ::belongsTo[products].name= MBP
        }

        ::hasOne[order_lines]: {
          quantity: 1,
          ::belongsTo[products].name= MBP
        }
      }]
      """

  Scenario: has one data is null
    Given Exists data "Order":
      | code |
      | S01  |
    When all follow tables:
      | products | order_lines |
    Then db should:
      """
      orders: [{
        code= S01
        ::hasOne[order_lines]::on[:id=order_id]= null,
        ::hasOne[order_lines]::on[:id]= null,
        ::hasOne[order_lines]::on[order_id]= null,
        ::hasOne[order_lines]= null
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
        ::hasOne[order_lines]: {
          quantity= 1,
          ::belongsTo[products].name= product1
        }

        ::hasOne[order_lines]::on[:pid]: {
          quantity= 2,
          ::belongsTo[products].name= product2
        }
      } {
        name= product2
        ::hasOne[order_lines]: {
          quantity= 2,
          ::belongsTo[products].name= product2
        }

        ::hasOne[order_lines]::on[:pid]: {
          quantity= 1,
          ::belongsTo[products].name= product1
        }
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
        ::hasOne[order_lines]: {
          quantity= 1,
          ::belongsTo[products].name= product1
        }
        ::hasOne[order_lines]::on[refid]: {
          quantity= 2,
          ::belongsTo[products].name= product2
        }
      } {
        name= product2
        ::hasOne[order_lines]: {
          quantity= 2,
          ::belongsTo[products].name= product2
        }
        ::hasOne[order_lines]::on[refid]: {
          quantity= 1,
          ::belongsTo[products].name= product1
        }
      }]
      """

  Scenario: has one do not allow more than one data
    Given Exists data "OrderLine":
      | quantity | order.code |
      | 1        | S01        |
      | 2        | S01        |
    When assert DB:
      """
      orders: [{
        ::hasOne[order_lines]: {
          quantity= 1
        }
      }]
      """
    Then raise error
    """
    message.trim: ```
                  orders: [{
                    ::hasOne[order_lines]: {
                            ^
                      quantity= 1
                    }
                  }]

                  Query more than one record

                  The root value was: com.github.leeonky.dal.extensions.jdbc.DataBase {}
                  ```
    """

  Scenario: raise error when invalid sql
    Given Exists data "OrderLine":
      | quantity | order.code |
      | 1        | S01        |
    When assert DB:
      """
      orders: [{
        ::hasOne[order_lines]::on[:id=not_exist]: {
          quantity= 1
        }
      }]
      """
    Then raise error
    """
    message.trim: ```
                  orders: [{
                    ::hasOne[order_lines]::on[:id=not_exist]: {
                                             ^
                      quantity= 1
                    }
                  }]

                  org.h2.jdbc.JdbcSQLSyntaxErrorException: Column "NOT_EXIST" not found; SQL statement:
                  select * from order_lines where ?=not_exist [42122-200]

                  The root value was: com.github.leeonky.dal.extensions.jdbc.DataBase {}
                  ```
    """
