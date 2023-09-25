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
