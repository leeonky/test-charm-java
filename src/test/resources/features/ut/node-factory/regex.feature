Feature: regex node

  Scenario: return null when does not match
    Given the following dal code:
    """
    100
    """
    Then got the following "regex" node:
    """
    : null
    """

  Scenario: compile to regex node
    Given the following dal code:
    """
    'hello' = /hello/
    """
    Then got the following "expression" node:
    """
    rightOperand: {
      class.simpleName: 'RegexNode'
      positionBegin: 10
      inspect: '/hello/'
    }
    """
    And evaluate result is:
    """
    = true
    """

  Scenario: regex does not match
    When assert by the following code:
    """
    'hello'= /unmatched/
    """
    Then failed with the following message:
    """
    expected ['hello'] matches /unmatched/ but was not
    """
    And got the following source code information:
    """
    'hello'= /unmatched/
             ^
    """

  Scenario: input value of 'equal to regex' must string type
    When assert by the following code:
    """
    100= /100/
    """
    Then failed with the following message:
    """
    Operator = before regex need a string input value
    """
    And got the following source code information:
    """
    100= /100/
       ^
    """

  Scenario: convert input value to string when 'match to regex'
    Then the following assertion should pass:
    """
    100: /100/
    """
