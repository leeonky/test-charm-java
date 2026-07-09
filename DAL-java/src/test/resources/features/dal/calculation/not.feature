Feature: not

  Scenario Outline: logic not operator
    When evaluate by:
    """
    <code>
    """
    Then the result should:
    """
    = <value>
    """
    Examples:
      | code   | value |
      | !false | true  |
      | !true  | false |

  Scenario: raise error when not operator is used with non-boolean value
    When evaluate by:
    """
    !'hello'
    """
    Then failed with the message:
    """
    Operand should be boolean but 'java.lang.String'
    """
    And got the following notation:
      """
      !'hello'
      ^
      """