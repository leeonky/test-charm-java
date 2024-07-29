Feature: query data

  Scenario: Query data use jfactory
    Given "Orders":
      | id | code |
      | 1  | SN1  |
    Then query data by jfactory:
    """
    = {
      Orders: [{
        id= 1
        code= SN1
      }]
    }
    """