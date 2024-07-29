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
      com.github.leeonky.dal.extensions.jfactory.Steps$1 {
          Orders: [
              com.github.leeonky.dal.extensions.jfactory.entity.Order {
                  code: java.lang.String <SN1>,
                  id: java.lang.Integer <1>
              }
          ]
      }
      ```
    """
