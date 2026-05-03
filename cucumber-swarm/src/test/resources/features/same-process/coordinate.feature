Feature: Master Worker Coordinate

  Scenario: launch master with empty feature
    When run cucumber with the following args:
      """
      '--glue'
      'steps'
      $path + 'features'
      """
    Then the task result should be:
      """
      : {
        code= 0
        stdout.normalize: ```
                          0 scenarios
                          0 steps
                          ```
      }
      """
    And the log should:
      """
      lines: [...
        'INFO: Master created'
      ...]
      """
