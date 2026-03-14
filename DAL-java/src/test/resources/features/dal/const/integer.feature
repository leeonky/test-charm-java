Feature: integer node

  Scenario Outline: null when does not match
    Given the following dal expression:
    """
    <code>
    """
    Then parse the following "integer" node:
    """
    : null
    """
    Examples:
      | code                    |
      | not starts with digital |
      | 1.1                     |
      | 1s                      |
      | 1y                      |
      | 1l                      |
      | 1bd                     |
      | 999999999999999         |

  Scenario Outline: supported format for integer parsing
    Given the following dal expression:
    """
     <code>
    """
    Then parse the following "integer" node:
    """
    : {
      class.simpleName: 'LiteralNode'
      inspect: '<inspect>'
      positionBegin: 1
    }
    """
    And last evaluated node result is:
    """
    : <evaluate>
    """
    Examples:
      | code  | inspect | evaluate |
      | 100   | 100     | 100      |
      | 0x100 | 256     | 256      |
      | -10   | -10     | -10      |

  Scenario Outline: delimiter between numbers
    When evaluate follow expression as "integer" node:
    """
     1<delimiter>
    """
    Then the result should:
    """
    : 1
    """
    Examples:
      | delimiter |
      | (         |
      | )         |
      | =         |
      | >         |
      | <         |
      | +         |
      | -         |
      | *         |
      | /         |
      | &         |
      | !         |
      | ,         |
      | [         |
      | ]         |
      | :         |
      | \|        |
      | \n        |
      | `TAB      |
      | `SPACE    |
