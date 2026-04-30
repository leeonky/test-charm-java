Feature: locating

  Scenario Outline: find all element by css
    When launch the following web page:
      """
      html
        body
          div unexpected
          .target expected1
          .target expected2
      """
    Then page in driver <driver> should:
      """
      css[.target]: [expected1 expected2]
      """
    And logs should:
      """
      : | level | message                             |
        | INFO  | Locating: css{html} => css{.target} |
        | INFO  | Found 2 elements                    |
      """
    Examples:
      | driver     |
      | selenium   |
      | playwright |

  Scenario Outline: find all element by caption
    When launch the following web page:
      """
      html
        body
          div unexpected
          label expected
          span expected
      """
    Then page in driver <driver> should:
      """
      caption[expected].tag[]= [label span]
      """
    And logs should:
      """
      : | level | message                                  |
        | INFO  | Locating: css{html} => caption{expected} |
        | INFO  | Found 2 elements                         |
      """
    Examples:
      | driver     |
      | selenium   |
      | playwright |

  Scenario Outline: find all element by xpath
    When launch the following web page:
      """
      html
        body
          div unexpected
          div(attr='a') expected1
          div(attr='a') expected2
      """
    Then page in driver <driver> should:
      """
      xpath["//div[@attr='a']"].text[]= [expected1 expected2]
      """
    And logs should:
      """
      : | level | message                                        |
        | INFO  | Locating: css{html} => xpath{//div[@attr='a']} |
        | INFO  | Found 2 elements                               |
      """
    Examples:
      | driver     |
      | selenium   |
      | playwright |

  Scenario Outline: find all element by placeholder
    When launch the following web page:
      """
      html
        body
          div unexpected
          div(placeholder='a') expected1
          div(placeholder='a') expected2
      """
    Then page in driver <driver> should:
      """
      placeholder[a].text[]= [expected1 expected2]
      """
    And logs should:
      """
      : | level | message                               |
        | INFO  | Locating: css{html} => placeholder{a} |
        | INFO  | Found 2 elements                      |
      """
    Examples:
      | driver     |
      | selenium   |
      | playwright |

