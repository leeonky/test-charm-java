Feature: assert db as data via jdbc

  Scenario: belongs to with where clause
    Given Exists data "OrderLine":
      | quantity | product.name |
      | 100      | MBP          |
    When all follow tables:
      | products | order_lines |
    Then db should:
      """
      tables.order_lines: [{
        quantity= 100,
        ::belongsTo.products::on[':product_id=id']: {
          name= MBP
        }
        ::belongsTo.products::where[':product_id=id']: {
          name= MBP
        }
        ::belongsTo.products@product_id: {
          name= MBP
        }
        ::belongsTo.products: {
          name= MBP
        }
      }]
      """

  Scenario: belongs to data is null
    Given Exists data "OrderLine":
      | product |
      |         |
    When all follow tables:
      | products | order_lines |
    Then db should:
      """
      tables.order_lines: [{
        ::belongsTo.products= null
      }]
      """

  Scenario: override default join column
    Given Exists data "Product":
      | id  | name |
      | 100 | MBP  |
    Given Exists data "OrderLine":
      | refId | product |
      | 100   |         |
    When all follow tables:
      | products | order_lines |
    Then db should:
      """
      tables.order_lines: [{
        ::belongsTo.products= null,
        ::belongsTo.products@refid: {
          name= MBP
        }
      }]
      """

#   join sql error
#  join more than one data