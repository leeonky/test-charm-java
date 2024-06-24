Feature: top n lit

  Scenario: top n for limited list
    Given the following json:
    """
    [1, 2, 3]
    """
    Then the following should pass:
    """
    ::top[2]= [1 2]
    """

  Scenario: top n for un-limited list
    Given a list from 1 to n
    Then the following should pass:
    """
    ::top[3]= [1 2 3]
    """

  Scenario: raise error when input is not list
    When evaluate by:
    """
    ::top[3]= [1 2 3]
    """
    Then failed with the message:
    """

    ::top[3]= [1 2 3]
      ^

    Invalid input value, expect a List but: null

    The root value was: null
    """
