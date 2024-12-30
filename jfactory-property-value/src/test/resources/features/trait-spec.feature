Feature: trait-spec

  Scenario: support trait spec in property
    When create "库存":
    """
    product(红色的 商品): {
      name= PC
    }
    size: A1
    count: 1
    """
    Then all "商品" should:
    """
    ::size= 1
    """
    Then all "库存" should:
    """
    : [{
      product: {
        color= red
        name= PC
      }
      size: A1
      count: 1
    }]
    """

  Scenario: support trait spec in list
    When create "商品":
    """
    stocks:     | remark |
    (无货 库存) | empty  |
    (满货 库存) | full   |
    """
    Then all "商品" should:
    """
    [0].stocks: | remark | count |
                | empty  | 0     |
                | full   | 100   |
    """

  Scenario: support trait spec in index specified list
    When create "商品":
    """
    stocks:      | remark |
    1(满货 库存) | full   |
    """
    Then all "商品" should:
    """
    [0].stocks: | remark | count |
                | ***            |
                | full   | 100   |
    """

  Scenario: legacy style trait spec in property
    When create "库存":
    """
    : {
      [product(红色的 商品)]: {
        name= PC
      }
      size: A1
      count: 1
    }
    """
    Then all "商品" should:
    """
    ::size= 1
    """
    Then all "库存" should:
    """
    : [{
      product: {
        color= red
        name= PC
      }
      size: A1
      count: 1
    }]
    """

  Scenario: legacy style trait spec in list
    When create "商品":
    """
    stocks: [{
      _: '(无货 库存)'
      remark: empty
    }, {
      _: '(满货 库存)'
      remark: full
    }]
    """
    Then all "商品" should:
    """
    [0].stocks: | remark | count |
                | empty  | 0     |
                | full   | 100   |
    """

  Scenario: support force creation
    When create "商品":
    """
    name: PC
    """
    When create "库存":
    """
    product!: {
      name= PC
    }
    """
    Then all "商品" should:
    """
    : | name |
      | PC   |
      | PC   |
    """

  Scenario: support force creation in list
    When create "商品":
    """
    name: PC
    """
    When create "品类":
    """
    products: | name |
            ! | PC   |
    """
    Then all "商品" should:
    """
    : | name |
      | PC   |
      | PC   |
    """

  Scenario: support force creation with legacy style
    When create "商品":
    """
    name: PC
    """
    When create "库存":
    """
    [product!]: {
      name= PC
    }
    """
    Then all "商品" should:
    """
    : | name |
      | PC   |
      | PC   |
    """

  Scenario: bug use trait spec in sub object
    When create
    """
    库存: {
      product(红色的 商品): {...}
    }
    """
    Then all "库存" should:
    """
    [0].product.color= red
    """

#  TODO bug
#  Scenario: bug use trait spec in sub list element
#    When create
#    """
#    商品: {
#      stocks[0](无货 库存): {...}
#    }
#    """
#    Then all "库存" should:
#    """
#    [0].count= 0
#    """
