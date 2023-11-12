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


#    legacy json mode use _