Feature: web ui
  Rule: find element
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
        findAll.css[.target].text[]= [expected1 expected2]
        """
      Examples:
        | driver     |
        | selenium   |
        | playwright |

    Scenario Outline: find all element by simple text
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
        findAll.text[expected].tag[]= [label span]
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
        findAll.xpath["//div[@attr='a']"].text[]= [expected1 expected2]
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
        findAll.placeholder[a].text[]= [expected1 expected2]
        """
      Examples:
        | driver     |
        | selenium   |
        | playwright |

    Scenario Outline: should only find elements from current node by css
      When launch the following web page:
        """
        html
          body
            .target unexpected
            .expected
              .target expected
        """
      Then page in driver <driver> should:
        """
        findAll.css[.expected][0].findAll.css[.target].text[]= [expected]
        """
      Examples:
        | driver     |
        | selenium   |
        | playwright |

    Scenario Outline: should only find elements from current node by text
      When launch the following web page:
        """
        html
          body
            div text
            .expected
              span text
        """
      Then page in driver <driver> should:
        """
        findAll.css[.expected][0].findAll.text[text].tag[]= [span]
        """
      Examples:
        | driver     |
        | selenium   |
        | playwright |

    Scenario Outline: should only find elements from current node by placeholder
      When launch the following web page:
        """
        html
        body
          div(placeholder='a') unexpected
          .expected
            div(placeholder='a') expected1
            div(placeholder='a') expected2
        """
      Then page in driver <driver> should:
        """
        findAll.css[.expected][0].findAll.placeholder[a].text[]= [expected1 expected2]
        """
      Examples:
        | driver     |
        | selenium   |
        | playwright |
