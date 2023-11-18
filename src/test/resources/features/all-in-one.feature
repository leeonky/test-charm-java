Feature: all in one

  Scenario: create any object in one step
    When create
    """
    商品: {
      name: book
      color: red
    }

    品类: | id | name |
          | C1 | it   |
          | C2 | xx   |
    """
    Then all "商品" should:
    """
    : | name | color |
      | book | red   |
    """
    Then all "品类" should:
    """
    : | id | name |
      | C1 | it   |
      | C2 | xx   |
    """

  Scenario: create duplicated spec
    When create
    """
    商品: {
      name: book
      color: red
    }

    商品: | name   | color |
          | iphone | white |
    """
    Then all "商品" should:
    """
    : | name   | color |
      | book   | red   |
      | iphone | white |
    """

#  TODO
#  Scenario: create with trait
#    When create
#    """
#    (红色的 商品): {
#      name: book
#    }
#    (红色的 商品): | name |
#                   | PC   |
#    """
#    Then all "商品" should:
#    """
#    : | name   | color |
#      | book   | red   |
#      | iphone | white |
#    """
#
#  Scenario: create duplicated object
#    When create
#    """
#    商品*3
#
#    商品*3: {
#      name: PC
#    }
#
#    商品*3: | name |
#            | PC   |
#            | iPad |
#
#    (红色的 商品)*3
#
#    (红色的 商品)*3: {}
#
#    (红色的 商品)*3: | name |
#                     | PC   |
#    """
#
#  Scenario: override trait and spec
#    When create
#    (红色的 商品): | name |
#                   | PC   |
#     (灰色的 商品) | iPad |
#    """
#    Then all "商品" should:
#    """
#    : | name   | color |
#      | book   | red   |
#      | iphone | white |
#    """
#  TODO
#  Scenario: sub object * count
#    When create "商品":
#    """
#    stocks * 3: {
#      size: xxl
#      count: 100
#    }
#
#    stocks * 3: {...}
#    """

#  TODO
#  Scenario: sub element trait for list/map and override
#    When create "商品":
#    """
#    stocks([库存]): | size | count |
#      (缺货的 库存) | A    | 100   |
#                    | B    | 200   |
#    """

#  TODO
#  Scenario: list mapping
#    When create "商品":
#    """
#    stocks.size[]: [A B]
#
#    stocks([库存]).size[]: [A B]
#
#    stocks([库存]).size(库存)[]: [A B] ?
#    """
