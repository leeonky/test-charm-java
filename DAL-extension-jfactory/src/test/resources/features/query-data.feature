Feature: query data

  Scenario: Query data use jfactory
    Given "Orders":
      | id | code |
      | 1  | SN1  |
    Then query data by jfactory:
    """
    : {
      Orders: [{
        id= 1
        code= SN1
      }]
    }
    """

  Scenario: dump data in repo and do not show empty data
    Given "Orders":
      | id | code |
      | 1  | SN1  |
    Then dumped jfactoy should be:
    """
    = ```
      org.testcharm.dal.extensions.jfactory.Steps$1 {
          Orders: org.testcharm.dal.runtime.StaticAdaptiveList [
              org.testcharm.dal.extensions.jfactory.entity.Order {
                  code: java.lang.String <SN1>,
                  id: java.lang.Integer <1>
              }
          ]
      }
      ```
    """

  Scenario: use adaptive list in query all result
    Given "Orders":
      | id | code |
      | 1  | SN1  |
    Then query data by jfactory:
    """
    Orders: {
      id= 1
      code= SN1
    }
    """

  Scenario: Eventually query data use jfactory
    Given "Orders" after 100ms:
      | id | code |
      | 1  | SN1  |
    Then query data by jfactory:
    """
    Orders::eventually: {
      id= 1
      code= SN1
    }
    """

