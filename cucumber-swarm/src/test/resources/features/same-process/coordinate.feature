Feature: Master Worker Coordinate

  Scenario: master and local worker with empty feature
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
        'INFO: Master created with 0 scenarios',
        /^INFO: Starting restful server on.*/
        'INFO: Restful server started'
        'INFO: Local worker<1> starting...'
        'INFO: Executor<1> started'
        'INFO: Requesting pickle...'
        'INFO: Received worker<1> pickle request'
        'INFO: No more pickles'
        'INFO: No pickle received'
        'INFO: Executor<1> ended'
      ...]
      """
    And the log should:
      """
      lines: [...
        'INFO: Pickle queue EMPTY'
        'INFO: Shutting down master...'
        'INFO: Waiting and collecting worker<1> exit status'
        'INFO: Worker<1> exit(0)'
        'INFO: Shutting down restful server...'
        'INFO: Restful server shut down'
        'INFO: Master shut down'
      ...]
      """

  Scenario: master and local worker with a no step-def scenario
    Given the feature file "no-step.feature":
      """
      Feature: no step

        Scenario: no step
          Given a step without implementation
      """
    When run cucumber with the following args:
      """
      '--glue'
      'steps'
      $path + 'features'
      """
    Then the task result should be:
      """
      : {
        code= 1
        stdout.normalize= ```
                          Undefined scenarios:
                            1) no step # file://$path$/features/no-step.feature:3

                          1 scenarios (1 undefined)
                          1 steps (1 undefined)

                          You can implement missing steps with the snippets below:

                          @Given("a step without implementation")
                          public void a_step_without_implementation() {
                              // Write code here that turns the phrase above into concrete actions
                              throw new io.cucumber.java.PendingException();
                          }
                          ```
      }
      """
    And the log should:
      """
      lines: [...
        'INFO: Master created with 1 scenarios',
        /^INFO: Starting restful server on.*/
        'INFO: Restful server started'
        'INFO: Local worker<1> starting...'
        'INFO: Executor<1> started'
        'INFO: Requesting pickle...'
        'INFO: Received worker<1> pickle request'
        'INFO: Send pickle<no-step.feature:3> to worker<1>'
        'INFO: Received pickle<no-step.feature:3>'
        'INFO: Requesting pickle...'
        'INFO: Received worker<1> pickle request'
        'INFO: No more pickles'
        'INFO: No pickle received'
      ...]
      """
    And the log should:
      """
      lines: [...
        'INFO: Shutting down master...'
        'INFO: Waiting and collecting worker<1> exit status'
        'INFO: Worker<1> exit(1)'
        'INFO: Shutting down restful server...'
        'INFO: Restful server shut down'
        'INFO: Master shut down'
      ...]
      """
