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
