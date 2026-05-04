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
        'INFO: Master created with 0 scenarios'
        'INFO: Starting restful server on 0:0:0:0:0:0:0:0:10084...'
        'INFO: Restful server started'
        'INFO: Worker<1> starting...'
        'INFO: Worker<1> started'
      ...]
      """
    And the log should:
      """
      lines: [...
        'INFO: Shutting down master...'
        'INFO: Collecting worker<1> exit status'
        'INFO: Worker<1> exit(0)'
        'INFO: Shutting down restful server...'
        'INFO: Restful server shut down'
        'INFO: Master shut down'
      ...]
      """
