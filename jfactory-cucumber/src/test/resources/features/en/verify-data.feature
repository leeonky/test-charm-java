Feature: verify data in one step

  Scenario: Prepare data with table
    Given Exists data "Product":
      | name |
      | book |
    Then should be:
    """
    商品: | name |
          | book |
    """
