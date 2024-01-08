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

  Scenario: json file
    Given root folder "/tmp/work/test/dir"
    Given a file "/tmp/work/test/dir/a.json"
    """
    { "key": "value" }
    """
    Given a file "/tmp/work/test/dir/b.JSON"
    """
    { "key": "VALUE" }
    """
    Then java.io.File "/tmp/work/test/dir/" should:
    """
    = {
      a.json= {key: value}
      b.JSON= {key: VALUE}
    }
    """
