Feature: syntax error expression

  Scenario Outline: missing operand2 with input data
    When evaluate by:
    """
    <opt>
    """
    Then failed with the message:
    """
    <message>
    """
    Examples:
      | opt  | message                      |
      | +    | Expect a value or expression |
      | -    | Expect a value or expression |
      | *    | Expect a value or expression |
      | /    | Expect a value or expression |
      | &&   | Expect a value or expression |
      | \|\| | Expect a value or expression |
      | and  | Expect a value or expression |
      | or   | Expect a value or expression |
      | ,    | Expect a value or expression |
      | >    | Expect a value or expression |
      | <    | Expect a value or expression |
      | >=   | Expect a value or expression |
      | <=   | Expect a value or expression |
      | !=   | Expect a value or expression |
#    verification will parse a empty string when no operand is missing
#      | =    | Expect a value or expression |
#      | :    | Expect a value or expression |

  Scenario Outline: missing operand2
    When evaluate by:
    """
    1 <opt>
    """
    Then failed with the message:
    """
    <message>
    """
    Examples:
      | opt  | message                      |
      | +    | Expect a value or expression |
      | -    | Expect a value or expression |
      | *    | Expect a value or expression |
      | /    | Expect a value or expression |
      | &&   | Expect a value or expression |
      | \|\| | Expect a value or expression |
      | and  | Expect a value or expression |
      | or   | Expect a value or expression |
      | ,    | Expect a value or expression |
      | >    | Expect a value or expression |
      | <    | Expect a value or expression |
      | >=   | Expect a value or expression |
      | <=   | Expect a value or expression |
      | !=   | Expect a value or expression |
#    verification will parse a empty string when no operand is missing
#      | =    | Expect a value or expression |
#      | :    | Expect a value or expression |

  Scenario: unexpected token after schema
    When evaluate by:
    """
    1 is Number .toString
    """
    Then failed with the message:
    """
    more than one expression
    """
    And got the following notation:
    """
    1 is Number .toString
                ^
    """
    When evaluate by:
    """
    1 is Number * 1
    """
    Then failed with the message:
    """
    Expect a value or expression
    """
    And got the following notation:
    """
    1 is Number * 1
                ^
    """

  Scenario: give an error when have space between property in object verification
    Given set error when ambiguous missed comma
    Given the following json:
    """
    {
      "length": 5,
      "value": "value",
      "list": [1, 2, 3, 4, 5]
    }
    """
    When evaluate by:
    """
    : {
      length= 'value'
      [length]
    }
    """
    Then failed with the message:
    """
    Missing a comma or remove whitespace.
    """
    And got the following notation:
    """
    : {
      length= 'value'
                    ^
      [length]
      ^
    }
    """
    When evaluate by:
    """
    : {
      length= .value
      [length]
    }
    """
    Then failed with the message:
    """
    Missing a comma or remove whitespace.
    """
    And got the following notation:
    """
    : {
      length= .value
                   ^
      [length]
      ^
    }
    """
    When evaluate by:
    """
    : {
      length= .value
      .length
    }
    """
    Then failed with the message:
    """
    Missing a comma or remove whitespace.
    """
    And got the following notation:
    """
    : {
      length= .value
                   ^
      .length
      ^
    }
    """
    When evaluate by:
    """
    : {
      length= .list
      ::size
    }
    """
    Then failed with the message:
    """
    Missing a comma or remove whitespace.
    """
    And got the following notation:
    """
    : {
      length= .list
                  ^
      ::size
      ^
    }
    """
    When evaluate by:
    """
    : {
      length= .list
      <<size>>
    }
    """
    Then failed with the message:
    """
    Missing a comma or remove whitespace.
    """
    And got the following notation:
    """
    : {
      length= .list
                  ^
      <<size>>
      ^
    }
    """
