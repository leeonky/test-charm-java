Feature: assert belongs to

  Scenario: belongs to with where clause
    Given Exists data "OrderLine":
      | quantity | product.name |
      | 100      | MBP          |
    And Exists data "Product":
      | name       |
      | unexpected |
    When all follow tables:
      | products | order_lines |
    Then db should:
      """
      order_lines: [{
        quantity= 100,
        ::belongsTo[products]::where[:product_id=id]: {
          name= MBP
        }
        ::belongsTo[products]::on[:product_id=id]: {
          name= MBP
        }
        ::belongsTo[products]: {
          name= MBP
        }
      }]
      """

#  Scenario: belongs to data is null
#    Given Exists data "OrderLine":
#      | product |
#      |         |
#    When all follow tables:
#      | products | order_lines |
#    Then db should:
#      """
#      order_lines: [{
#        ::belongsTo.products= null,
#        ::belongsTo.products::on[:product_id]= null,
#        ::belongsTo.products::on[id]= null,
#        ::belongsTo.products::on[:product_id=id]= null
#      }]
#      """
#
#  Scenario: override default join column
#    Given Exists data "Product":
#      | id  | name       |
#      | 100 | MBP        |
#      | 101 | unexpected |
#    Given Exists data "OrderLine":
#      | refId | product |
#      | 100   |         |
#    When all follow tables:
#      | products | order_lines |
#    Then db should:
#      """
#      order_lines: [{
#        ::belongsTo.products= null,
#        ::belongsTo.products::on[:refid]: {
#          name= MBP
#        }
#      }]
#      """
#
#  Scenario: override default primary key column
#    Given Exists data "Product":
#      | id  | pid | name     |
#      | 1   | 2   | original |
#      | 100 | 1   | newName  |
#    Given Exists data "OrderLine":
#      | refId | product.name |
#      | 100   | original     |
#    Then db should:
#      """
#      order_lines: [{
#        ::belongsTo.products::on[pid]: {
#          name= newName
#        }
#      }]
#      """
#
  Scenario: belongs to do not allow more than one data
    Given Exists data "Product":
      | pid |
      | 100 |
      | 100 |
    Given Exists data "OrderLine":
      | refId |
      | 100   |
    When assert DB:
      """
      order_lines: [{
        ::belongsTo.products::on[:refid=pid]: {
          name= any
        }
      }]
      """
    Then raise error
    """
    message.trim: ```
                  order_lines: [{
                    ::belongsTo.products::on[:refid=pid]: {
                                            ^
                      name= any
                    }
                  }]

                  Query more than one record

                  The root value was: com.github.leeonky.dal.extensions.jdbc.DataBase {}
                  ```
    """

#  Scenario: raise error when invalid sql
#    Given Exists data "Product":
#      | pid |
#      | 100 |
#      | 100 |
#    Given Exists data "OrderLine":
#      | refId |
#      | 100   |
#    When assert DB:
#      """
#      order_lines: [{
#        ::belongsTo.products::on[:refid=pidx]: {
#          name= any
#        }
#      }]
#      """
#    Then raise error
#    """
#    message.trim: ```
#                  order_lines: [{
#                    ::belongsTo.products::on[:refid=pidx]: {
#                                            ^
#                      name= any
#                    }
#                  }]
#
#                  org.h2.jdbc.JdbcSQLSyntaxErrorException: Column "PIDX" not found; SQL statement:
#                  select * from products where ?=pidx [42122-200]
#
#                  The root value was: com.github.leeonky.dal.extensions.jdbc.DataBase {}
#                  ```
#    """
