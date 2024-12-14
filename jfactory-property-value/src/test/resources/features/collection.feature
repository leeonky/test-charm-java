Feature: collection

  Scenario: create object list with given spec
    When create some "商品":
    """
    : | name | color |
      | book | red   |
    """
    Then all "商品" should:
    """
    : | name | color |
      | book | red   |
    """

  Scenario: ignore : create object list with given spec
    When create some "商品":
    """
    | name | color |
    | book | red   |
    """
    Then all "商品" should:
    """
    : | name | color |
      | book | red   |
    """

  Scenario: create object list with given spec use []
    When create some "商品":
    """
    : [{
      name: book
      color: red
    }]
    """
    Then all "商品" should:
    """
    : | name | color |
      | book | red   |
    """

  Scenario: create object list with given spec use [] ignore :
    When create some "商品":
    """
    [{
      name: book
      color: red
    }]
    """
    Then all "商品" should:
    """
    : | name | color |
      | book | red   |
    """

  Scenario: create single object in collection style
    When create some "商品":
    """
    name: book
    color: red
    """
    Then all "商品" should:
    """
    : | name | color |
      | book | red   |
    """

  Scenario: ignore one cell value in table
    When create some "商品":
    """
    | name   | color |
    | iphone | *     |
    | book   | red   |
    """
    Then all "商品" should:
    """
    : | name   | color     |
      | iphone | /color.*/ |
      | book   | red       |
    """

  Scenario: ignore one row in table
    When create some "商品":
    """
    | name   | color |
    | ***            |
    | book   | red   |
    """
    Then all "商品" should:
    """
    : | name   | color     |
      | iphone | /color.*/ |
      | book   | red       |
    """

  Scenario: ignore one row in collection property table
    When create some "商品":
    """
    stocks: | size | count |
            | ***          |
            | B    | 100   |
    """
    Then all "商品" should:
    """
    [0].stocks: [null {
      size: B
      count: 100
    }]
    """
