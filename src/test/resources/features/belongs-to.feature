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
#        ::belongsTo.products::where[':product_id=id']: {
#          name= MBP
#        }
#        ::belongsTo.products@product_id: {
#          name= MBP
#        }
#        ::belongsTo.products: {
#          name= MBP
#        }
      }]
      """
