Feature: dal

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
