Feature: assert db as data via jdbc

  Scenario: list all configured tables
    Given all follow tables:
      | products | orders |
    Then db should:
      """
      tables= {
        products: {...}
        orders: {...}
      }
      """

  Scenario: list all configured views
    Given all follow views:
      | products | orders |
    Then db should:
      """
      views= {
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
      tables.products::size=1
      """

  Scenario: assert table data
    Given Exists data "Product":
      | id | name |
      | 10 | MBP  |
    When all follow tables:
      | products |
    Then db should:
      """
      tables.products: [{
        id= 10L
        name= MBP
      }]
      """
