Feature: parse yaml

  Scenario: parse yaml string
    Then the following should pass:
    """
    ```
    key: value
    ```.yaml= { key= value }
    """

    And the following should pass:
    """
    ``` YAML
    key: value
    ```= { key= value }
    """
    And the following should pass:
    """
    ``` yaml
    key: value
    ```= { key= value }
    """

  Scenario: yaml file
    Given root folder "/tmp/work/test/dir"
    Given a file "/tmp/work/test/dir/a.yaml"
    """
    key: value
    """
    Given a file "/tmp/work/test/dir/b.yaml"
    """
    key: VALUE
    """
    Then java.io.File "/tmp/work/test/dir/" should:
    """
    = {
      a.yaml= {key: value}
      b.yaml= {key: VALUE}
    }
    """
