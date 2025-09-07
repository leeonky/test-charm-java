Feature: root

  Scenario: ref input root value by ::root
    Given the following json:
      """
      {
        "key": {
          "key": "value"
        }
      }
      """
    Then the following verification should pass:
      """
      ::root= {key= {key= value}}
      """
    When evaluate by:
      """
      ::root: {key= any}
      """
    Then failed with the message:
      """
      Expected to be equal to: java.lang.String
                               ^
      <any>
      Actual: {
              ^
          key: java.lang.String <value>
      }
      """
    And got the following notation:
      """
      ::root: {key= any}
                    ^
      """

  Scenario: ::root in object scope
    Given the following json:
      """
      {
        "key": {
          "key": "value"
        }
      }
      """
    Then the following verification should pass:
      """
      : {
        key: {
          ::root= {key= {key= value}}
        }
      }
      """
    When evaluate by:
      """
      : {
        key: {
          ::root= {key= any}
        }
      }
      """
    Then failed with the message:
      """
      Expected to be equal to: java.lang.String
                               ^
      <any>
      Actual: {
              ^
          key: java.lang.String <value>
      }
      """
    And got the following notation:
      """
      : {
        key: {
          ::root= {key= any}
                        ^
        }
      }
      """

