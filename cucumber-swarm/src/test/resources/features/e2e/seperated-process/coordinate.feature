Feature: Master Worker Coordinate

  Scenario: master and 1 remote worker with empty feature
    When run cucumber in remote mode with the following args:
      """
      '--remote-worker-count'
      '1'
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
    And the master log should:
      """
      lines: [...
        'INFO: Master created with 0 scenarios',
        /^INFO: Starting restful server on.*/
        'INFO: Restful server started'
        {::should.startsWith: 'INFO: Remote worker<1> starting'}
        'FINE: Received worker<1> pickle request'
        'FINE: No more pickles'
      ...]
      """
    And the master log should:
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
    And the worker 1 log should:
      """
      lines: [...
        'INFO: Executor<1> started'
        'FINE: Requesting pickle...'
        'FINE: No pickle received'
        'INFO: Executor<1> ended'
      ...]
      """

  Scenario: master and 1 remote worker with a no step-def scenario
    Given the feature file "no-step.feature":
      """
      Feature: no step

        Scenario: no step
          Given a step without implementation
      """
    When run cucumber in remote mode with the following args:
      """
      '--remote-worker-count'
      '1'
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
    And the master log should:
      """
      lines: [...
        'INFO: Master created with 1 scenarios',
        /^INFO: Starting restful server on.*/
        'INFO: Restful server started'
        {::should.startsWith: 'INFO: Remote worker<1> starting'}
        'FINE: Received worker<1> pickle request'
        'FINE: Send pickle<$r_path$/features/no-step.feature:3> to worker<1>'
        'FINE: Received worker<1> pickle request'
        'FINE: No more pickles'
      ...]
      """
    And the master log should:
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
    And the worker 1 log should:
      """
      lines: [...
        'INFO: Executor<1> started'
        'FINE: Requesting pickle...'
        'FINE: Received pickle<$r_path$/features/no-step.feature:3>'
        'FINE: Requesting pickle...'
        'FINE: No pickle received'
        'INFO: Executor<1> ended'
      ...]
      """

  Scenario: master and 1 remote worker with a one passed step scenario
    Given the feature file "test.feature":
      """
      Feature: test

        Scenario: test
          Given a step with implementation
      """
    Given the following class definition:
      """
      package steps;
      import io.cucumber.java.en.*;

      public class Steps {

        @Given("a step with implementation")
        public void a_step_with_implementation() {
          System.out.println("step called");
        }
      }
      """
    When run cucumber in remote mode with the following args:
      """
      '--remote-worker-count'
      '1'
      '--glue'
      'steps'
      $path + 'features'
      """
    Then the task result should be:
      """
      : {
        code= 0
        stdout.normalize= ```
                          1 scenarios (1 passed)
                          1 steps (1 passed)
                          ```
      }
      """
    And the log should:
      """
      lines: [...
        'INFO: Master created with 1 scenarios',
        /^INFO: Starting restful server on.*/
        'INFO: Restful server started'
        'FINE: Received worker<1> pickle request'
        'FINE: Send pickle<$r_path$/features/test.feature:3> to worker<1>'
        'FINE: Received worker<1> pickle request'
        'FINE: No more pickles'
      ...]
      """
    And the log should:
      """
      lines::should[]: [...
        {
          contains: 'FINE: Received worker<1> event: {"type":"io.cucumber.plugin.event.TestCaseStarted","data":{"timeInstant"'
          contains: '"testCase":"$r_path$/features/test.feature:3"'
        }
        { contains: 'FINE: Forwarding event: io.cucumber.plugin.event.TestCaseStarted' }
      ...]
      """
    And the log should:
      """
      lines: [...
        'INFO: Shutting down master...'
        'INFO: Waiting and collecting worker<1> exit status'
        'INFO: Worker<1> exit(0)'
        'INFO: Shutting down restful server...'
        'INFO: Restful server shut down'
        'INFO: Master shut down'
      ...]
      """
    And the worker 1 log should:
      """
      lines: [...
        'INFO: Executor<1> started'
        'FINE: Requesting pickle...'
        'FINE: Received pickle<$r_path$/features/test.feature:3>'
        'FINE: Requesting pickle...'
        'FINE: No pickle received'
        'INFO: Executor<1> ended'
      ...]
      """
    And the worker 1 log should:
      """
      lines::should[]: [...
        {
          contains: 'FINE: Forwarding event: {"type":"io.cucumber.plugin.event.TestCaseStarted","data":{"timeInstant"'
          contains: '"testCase":"$r_path$/features/test.feature:3"'
        }
      ...]
      """

  Scenario: do not hang when worker ready error
    Given the feature file "test.feature":
      """
      Feature: test

        Scenario: test
          Given a step with implementation
      """
    Given the following class definition:
      """
      package steps;
      import io.cucumber.java.en.*;

      public class Steps {

        @Given("a step with implementation")
        public void a_step_with_implementation() {
          System.out.println("step called");
        }
      }
      """
    When run cucumber in remote mode with the following args:
      """
      '--worker-timeout'
      '1'
      '--remote-worker-count'
      '1'
      '--swarm-host'
      'not-exist.com'
      '--glue'
      'steps'
      $path + 'features'
      """
    Then the task result should be:
      """
      : {
        code= 1
      }
      """

  Scenario: raise error with host info when got io error in rest api request
    Given the feature file "test.feature":
      """
      Feature: test
        Scenario: test
          Given a step with implementation
      """
    Given the following class definition:
      """
      package steps;
      import io.cucumber.java.en.*;

      public class Steps {

        @Given("a step with implementation")
        public void a_step_with_implementation() {
          System.out.println("step called");
        }
      }
      """
    When run cucumber in remote mode with the following args:
      """
      '--worker-timeout'
      '1'
      '--remote-worker-count'
      '1'
      '--swarm-host'
      'not-exist.com'
      '--swarm-port'
      '20000'
      '--glue'
      'steps'
      $path + 'features'
      """
    Then the task result should be:
      """
      : { code= 1 }
      """
    And the worker 1 log should:
      """
      lines: [...
        'SEVERE: Failed to POST to http://not-exist.com:20000/ready'
      ...]
      """
