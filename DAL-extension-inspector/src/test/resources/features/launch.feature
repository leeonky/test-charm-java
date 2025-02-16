Feature: launch

  Scenario: launch server and page
    When launch inspector web server
    And  launch inspector web page
    Then you can see page "DAL inspector"
