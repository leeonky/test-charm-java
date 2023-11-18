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
