Feature: web ui
  Rule: find element
    Scenario: find all element by css
      When launch the following web page:
      """
      <html>
      <body>
        <div>unexpected</div>
        <div class='target'>expected1</div>
        <div class='target'>expected2</div>
      </body>
      </html>
      """
      Then page in driver selenium should:
      """
      findAll.css[.target].text[]= [expected1 expected2]
      """
