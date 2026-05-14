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

  Scenario: master and local worker with a one passed step scenario
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
        stdout.normalize= ```
                          step called

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
        'INFO: Local worker<1> starting...'
        'INFO: Executor<1> started'
        'INFO: Requesting pickle...'
        'INFO: Received worker<1> pickle request'
        'INFO: Send pickle<test.feature:3> to worker<1>'
        'INFO: Received pickle<test.feature:3>'
        'INFO: Requesting pickle...'
        'INFO: Received worker<1> pickle request'
        'INFO: No more pickles'
        'INFO: No pickle received'
      ...]
      """
    And the log should:
      """
      lines::should[]: [...
        {
          contains: 'Forwarding event: {"type":"io.cucumber.plugin.event.TestCaseStarted","data":{"timeInstant"'
          contains: '"testCase":"test.feature:3"'
        }
        {
          contains: 'Received worker<1> event: {"type":"io.cucumber.plugin.event.TestCaseStarted","data":{"timeInstant"'
          contains: '"testCase":"test.feature:3"'
        }
        { contains: 'Forwarding event: io.cucumber.plugin.event.TestCaseStarted' }
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

  Scenario: master and local worker with a one failed step scenario
    Given the feature file "test.feature":
      """
      Feature: test

        Scenario: test
          Given a failed step
      """
    Given the following class definition:
      """
      package steps;
      import io.cucumber.java.en.*;

      public class Steps {

        @Given("a failed step")
        public void a_step_with_implementation() {
          System.out.println("step called");
          throw new AssertionError("step failed");
        }
      }
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
                          step called

                          Failed scenarios:
                            1) test # file://$path$/features/test.feature:3
                                 java.lang.AssertionError: step failed
                                 \tat steps.Steps.a_step_with_implementation(Steps.java:10)
                                 \tat ✽.a failed step(file://$path$/features/test.feature:4)


                          1 scenarios (1 failed)
                          1 steps (1 failed)
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
        'INFO: Send pickle<test.feature:3> to worker<1>'
        'INFO: Received pickle<test.feature:3>'
        'INFO: Requesting pickle...'
        'INFO: Received worker<1> pickle request'
        'INFO: No more pickles'
        'INFO: No pickle received'
      ...]
      """
    And the log should:
      """
      lines::should[]: [...
        {
          contains: 'Forwarding event: {"type":"io.cucumber.plugin.event.TestCaseStarted","data":{"timeInstant"'
          contains: '"testCase":"test.feature:3"'
        }
        {
          contains: 'Received worker<1> event: {"type":"io.cucumber.plugin.event.TestCaseStarted","data":{"timeInstant"'
          contains: '"testCase":"test.feature:3"'
        }
        { contains: 'Forwarding event: io.cucumber.plugin.event.TestCaseStarted' }
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
