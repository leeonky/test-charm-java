Feature: list node

  Scenario: return null when does not match
    Given the following dal code xx:
    """
    +
    """
    Then got the following "list" node xx:
    """
    : null
    """

  Scenario: support empty list
    Given the following dal code xx:
    """
     []
    """
    Then got the following "list" node xx:
    """
    : {
      class.simpleName: 'ListNode'
      inspect: '[]'
      positionBegin: 1
    }
    """

  Scenario: support one element list
    Given the following dal code xx:
    """
     [1]
    """
    Then got the following "list" node xx:
    """
    : {
      class.simpleName: 'ListNode'
      inspect: '[1]'
      positionBegin: 1
      expressions.inspect: ['[0] : 1']
    }
    """

  Scenario: support two elements list
    Given the following dal code xx:
    """
     [1 2]
    """
    Then got the following "list" node xx:
    """
    : {
      class.simpleName: 'ListNode'
      inspect: '[1 2]'
      positionBegin: 1
      expressions.inspect: [
        '[0] : 1'
        '[1] : 2'
      ]
    }
    """

  Scenario: raise error when no closing bracket
    Given the following dal code xx:
    """
    [1
    """
    Then failed to get "list" node with the following message xx:
    """
    should end with `]`
    """
    And got the following source code information xx:
    """
    [1
      ^
    """

  Scenario: raise error when element is invalid
    Given the following dal code xx:
    """
    [ + ]
    """
    Then failed to get "list" node with the following message xx:
    """
    expect a value or expression
    """
    And got the following source code information xx:
    """
    [ + ]
      ^
    """

  Scenario: support incomplete List
    Given the following dal code xx:
    """
     [1 ...]
    """
    Then got the following "list" node xx:
    """
    : {
      class.simpleName: 'ListNode'
      inspect: '[1 ...]'
      positionBegin: 1
      expressions.inspect: [
        '[0] : 1'
      ]
    }
    """
