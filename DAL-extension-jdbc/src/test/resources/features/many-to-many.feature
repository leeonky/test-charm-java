Feature: many-to-many

  Scenario: has many with join table
    Given Exists data "Product":
      | name |
      | p1   |
    Given Exists "Product.name[p1].skus" as data "Sku":
      | name |
      | s1   |
      | s2   |
#    unexpected
    Given Exists data "Product":
      | name |
      | any  |
    Given Exists "Product.name[any].skus" as data "Sku":
      | name |
      | any  |
    Then db should:
      """
      products[0]: {
        ::hasMany[skus]::through[sku_products]: | name |
                                                | s1   |
                                                | s2   |

        ::hasMany[skus]::on[id]::through[sku_products]: | name |
                                                        | s1   |
                                                        | s2   |

        ::hasMany[skus]::on[id]::through[sku_products.sku_id]: | name |
                                                               | s1   |
                                                               | s2   |

        ::hasMany[skus]::on[id]::through[sku_products.sku_id]::on[product_id=:id]: | name |
                                                                                   | s1   |
                                                                                   | s2   |
      }
      """
    When define to to hasMany skus method on products row
    Then db should:
      """
      products[0]: {
        skus: | name |
              | s1   |
              | s2   |

        skusInSql: | name |
                   | s1   |
                   | s2   |
      }
      """


  Scenario: has one with join table
    Given Exists data "Product":
      | name |
      | p1   |
    Given Exists "Product.name[p1].skus" as data "Sku":
      | name |
      | s1   |
    Then db should:
      """
      products[0]: {
        ::hasOne[skus]::through[sku_products].name= s1
      }
      """
