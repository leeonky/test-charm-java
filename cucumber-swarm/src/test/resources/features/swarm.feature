Feature: swarm

  Scenario: run cucumber master with empty feature folders
    When run cucumber with the following args:
      """
      $path + 'features'
      """
    Then the task result should be:
      """
      : {
        code= 0
        stdout::should.startsWith: ```

                                   0 scenarios
                                   0 steps
                                   ```
      }
      """