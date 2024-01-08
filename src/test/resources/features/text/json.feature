Feature: parse json

  Scenario: parse json string
    Then the following should pass:
    """
    '{"key": "value"}'.json= { key= value }
    """

    And the following should pass:
    """
    ``` JSON
    {"key": "value"}
    ```= { key= value }
    """
    And the following should pass:
    """
    ``` json
    {"key": "value"}
    ```= { key= value }
    """
