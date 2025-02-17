Feature: basic
  Rule: web server ready, web page opened

    Background:
      Given launch inspector web server
      And  launch inspector web page

    Scenario: launch server and page
      Then you can see page "DAL inspector"

    Scenario: execute expression and get result
      When try dal on page:
      """
      1= 2
      """
      Then yon can see the same expression
