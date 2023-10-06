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
    message.trim= ```
                  products: [{
                    notExist= {...}
                    ^
                  }]

                  Get property `notExist` failed, property can be:
                    1. public field
                    2. public getter
                    3. public no args method
                    4. Map key value
                    5. customized type getter
                    6. static method extension
                  java.lang.RuntimeException: No such column: notExist

                  The root value was: com.github.leeonky.dal.extensions.jdbc.DataBase {
                      products: [
                          com.github.leeonky.dal.extensions.jdbc.DataBase$Row {
                              id: java.lang.Long <65>,
                              createdat: java.sql.Timestamp <1996-01-23 08:01:05.0>,
                              name: java.lang.String <name#65>,
                              pid: java.lang.Long <65>,
                              price: java.lang.Integer <65>
                          }
                      ]
                  }
                  ```
    """
