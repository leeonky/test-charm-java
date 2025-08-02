Feature: assert db as data via jdbc

  Scenario: list all configured tables
    Given all follow tables:
      | products | orders |
    Then db should:
      """
      : {
        products: {...}
        orders: {...}
      }
      """

  Scenario: assert table size
    Given Exists 1 data "Product"
    When all follow tables:
      | products |
    Then db should:
      """
      products::size=1
      """

  Scenario: assert whole table data
    Given Exists data "Product":
      | id | name |
      | 10 | MBP  |
    Then db should:
      """
      products: [{
        id= 10L
        name= MBP
      }]
      """

  Scenario: assert table with where and select
    Given Exists data "Product":
      | name | pid |
      | MBP  | 1   |
      | iPod | 2   |
    Then db should:
      """
      : {
        products::where[name='MBP']: [{
          name= MBP
        }]

        products::select[name as n].n[]: [MBP iPod]
      }
      """

  Scenario: assert table with many wheres
    Given Exists data "Product":
      | name       | pid |
      | MBP        | 1   |
      | iPod       | 2   |
      | iPod       | 3   |
      | unexpected | 3   |
    Then db should:
      """
      : {
        products: {
          ::where[name='MBP']: [{
            name= MBP
          }]
          ::where[name='iPod']::where[pid=3]: [{
            name= iPod
          }]
        }
      }
      """

  Scenario: define data on Row
    Given Exists data "Product":
      | name |
      | mbp  |
    And define to to upper name method on products row
    Then db should:
      """
      : {
        products: [{
          upperName= MBP
        }]
      }
      """

  Scenario: raise error when no such column
    Given Exists 1 data "Product"
    When assert DB:
      """
      products: [{
        notExist= {...}
      }]
      """
    Then raise error
    """
    message.trim::should.startsWith: ```
                  products: [{
                    notExist= {...}
                    ^
                  }]

                  java.lang.RuntimeException: No such column: notExist
                  ```
    """

  Scenario: dump table as a 'table'
    Given Exists data "Order":
      | id | code |
      | 10 | S01  |
    And all follow tables:
      | orders |
    Then dumped data base should:
    """
    = ```
      DataBase[jdbc:h2:mem:test] {
          orders:
              | id | code |
              | 10 |  S01 |
      }
      ```
    """

  Scenario: do not dump table when table is empty
    And all follow tables:
      | orders |
    Then dumped data base should:
    """
    = ```
      DataBase[jdbc:h2:mem:test] {}
      ```
    """
