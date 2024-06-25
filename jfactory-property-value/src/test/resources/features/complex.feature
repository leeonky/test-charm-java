Feature: complex style

  Scenario: create with list mapping
    When create "商品":
    """
    stocks.size[]: [A B]
    stocks.remark[]: [x y]
    """
    Then all "商品" should:
    """
    [0].stocks: | size | remark |
                | A    | x      |
                | B    | y      |
    """

  Scenario: create with list mapping in row header
    When create "商品":
    """
    stocks:  | [0] |
      size[] | A   |
    """
    Then all "商品" should:
    """
    [0].stocks.size[]: [A]
    """

  Scenario: create with list mapping in column header
    When create "商品":
    """
            | size[] |
    stocks: | [A]    |
    """
    Then all "商品" should:
    """
    [0].stocks.size[]: [A]
    """

  Scenario: create with group
    When create "商品":
    """
    stocks.size[]<<0 1>>: A
    """
    Then all "商品" should:
    """
    [0].stocks.size[]: [A A]
    """

  Scenario: create with group in row header
    When create "商品":
    """
                  |size |
    stocks<<0 1>> | A   |
    """
    Then all "商品" should:
    """
    [0].stocks.size[]: [A A]
    """

  Scenario: create with group in column header
    When create "商品":
    """
            |<<0 1>>.size |
    stocks: | A           |
    """
    Then all "商品" should:
    """
    [0].stocks.size[]: [A A]
    """
