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
        'INFO: Received worker<1> ready signal'
        'INFO: Worker<1> is ready'
        'INFO: Executor<1> started'
        'FINE: Requesting pickle...'
        'FINE: Received worker<1> pickle request'
        'FINE: No more pickles'
        'FINE: No pickle received'
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
        'FINE: Requesting pickle...'
        'FINE: Received worker<1> pickle request'
        'FINE: Send pickle<$r_path$/features/no-step.feature:3> to worker<1>'
        'FINE: Received pickle<$r_path$/features/no-step.feature:3>'
        'FINE: Requesting pickle...'
        'FINE: Received worker<1> pickle request'
        'FINE: No more pickles'
        'FINE: No pickle received'
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
        'FINE: Requesting pickle...'
        'FINE: Received worker<1> pickle request'
        'FINE: Send pickle<$r_path$/features/test.feature:3> to worker<1>'
        'FINE: Received pickle<$r_path$/features/test.feature:3>'
        'FINE: Requesting pickle...'
        'FINE: Received worker<1> pickle request'
        'FINE: No more pickles'
        'FINE: No pickle received'
      ...]
      """
    And the log should:
      """
      lines::should[]: [...
        {
          contains: 'FINE: Forwarding event: {"type":"io.cucumber.plugin.event.TestCaseStarted","data":{"timeInstant"'
          contains: '"testCase":"$r_path$/features/test.feature:3"'
        }
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
        'FINE: Requesting pickle...'
        'FINE: Received worker<1> pickle request'
        'FINE: Send pickle<$r_path$/features/test.feature:3> to worker<1>'
        'FINE: Received pickle<$r_path$/features/test.feature:3>'
        'FINE: Requesting pickle...'
        'FINE: Received worker<1> pickle request'
        'FINE: No more pickles'
        'FINE: No pickle received'
      ...]
      """
    And the log should:
      """
      lines::should[]: [...
        {
          contains: 'FINE: Forwarding event: {"type":"io.cucumber.plugin.event.TestCaseStarted","data":{"timeInstant"'
          contains: '"testCase":"$r_path$/features/test.feature:3"'
        }
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
        'INFO: Worker<1> exit(1)'
        'INFO: Shutting down restful server...'
        'INFO: Restful server shut down'
        'INFO: Master shut down'
      ...]
      """

  Scenario: master and local worker with a skipped step scenario
    Given the feature file "test.feature":
      """
      Feature: test

        Scenario: test
          Given a failed step
          And a skipped step
      """
    Given the following class definition:
      """
      package steps;
      import io.cucumber.java.en.*;

      public class Steps {

        @Given("a failed step")
        public void a_failed_step() {
          throw new RuntimeException("step failed");
        }

        @Given("a skipped step")
        public void a_skipped_step() {
          System.out.println("step should be skipped");
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
                          Failed scenarios:
                            1) test # file://$path$/features/test.feature:3
                                 java.lang.RuntimeException: step failed
                                 \tat steps.Steps.a_failed_step(Steps.java:9)
                                 \tat ✽.a failed step(file://$path$/features/test.feature:4)


                          1 scenarios (1 failed)
                          2 steps (1 skipped, 1 failed)
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
        'FINE: Requesting pickle...'
        'FINE: Received worker<1> pickle request'
        'FINE: Send pickle<$r_path$/features/test.feature:3> to worker<1>'
        'FINE: Received pickle<$r_path$/features/test.feature:3>'
        'FINE: Requesting pickle...'
        'FINE: Received worker<1> pickle request'
        'FINE: No more pickles'
        'FINE: No pickle received'
      ...]
      """
    And the log should:
      """
      lines::should[]: [...
        {
          contains: 'FINE: Received worker<1> event: {"type":"io.cucumber.plugin.event.TestStepFinished"'
          contains: '"status":"FAILED"'
        }
        {
          contains: 'FINE: Received worker<1> event: {"type":"io.cucumber.plugin.event.TestStepFinished"'
          contains: '"status":"SKIPPED"'
        }
        { contains: 'FINE: Forwarding event: io.cucumber.plugin.event.TestStepFinished' }
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

  Scenario: master and local worker with a pending step scenario
    Given the feature file "test.feature":
      """
      Feature: test

        Scenario: test
          Given a pending step
      """
    Given the following class definition:
      """
      package steps;
      import io.cucumber.java.PendingException;
      import io.cucumber.java.en.*;

      public class Steps {

        @Given("a pending step")
        public void a_pending_step() {
          throw new PendingException("step pending");
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
                          Pending scenarios:
                            1) test # file://$path$/features/test.feature:3
                                 io.cucumber.java.PendingException: step pending
                                 \tat steps.Steps.a_pending_step(Steps.java:10)
                                 \tat ✽.a pending step(file://$path$/features/test.feature:4)


                          1 scenarios (1 pending)
                          1 steps (1 pending)
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
        'FINE: Requesting pickle...'
        'FINE: Received worker<1> pickle request'
        'FINE: Send pickle<$r_path$/features/test.feature:3> to worker<1>'
        'FINE: Received pickle<$r_path$/features/test.feature:3>'
        'FINE: Requesting pickle...'
        'FINE: Received worker<1> pickle request'
        'FINE: No more pickles'
        'FINE: No pickle received'
      ...]
      """
    And the log should:
      """
      lines::should[]: [...
        {
          contains: 'FINE: Received worker<1> event: {"type":"io.cucumber.plugin.event.TestStepFinished"'
          contains: '"status":"PENDING"'
        }
        { contains: 'FINE: Forwarding event: io.cucumber.plugin.event.TestStepFinished' }
        {
          contains: 'FINE: Received worker<1> event: {"type":"io.cucumber.plugin.event.TestCaseFinished"'
          contains: '"status":"PENDING"'
        }
        { contains: 'FINE: Forwarding event: io.cucumber.plugin.event.TestCaseFinished' }
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

  Scenario: master and local worker with an ambiguous step scenario
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

        @Given("^.* implementation$")
        public void another_a_step_with_implementation() {
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
        code= 1
        stdout.normalize::should.startsWith: ```
                                             Ambiguous scenarios:
                                               1) test # file://$path$/features/test.feature:3
                                                    io.cucumber.core.runner.AmbiguousStepDefinitionsException: "a step with implementation" matches more than one step definition:
                                                      "^.* implementation$" in steps.Steps.another_a_step_with_implementation()
                                                      "a step with implementation" in steps.Steps.a_step_with_implementation()
                                             ```
        stdout.normalize::should.endsWith: ```
                                           1 scenarios (1 ambiguous)
                                           1 steps (1 ambiguous)
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
        'FINE: Requesting pickle...'
        'FINE: Received worker<1> pickle request'
        'FINE: Send pickle<$r_path$/features/test.feature:3> to worker<1>'
        'FINE: Received pickle<$r_path$/features/test.feature:3>'
        'FINE: Requesting pickle...'
        'FINE: Received worker<1> pickle request'
        'FINE: No more pickles'
        'FINE: No pickle received'
      ...]
      """
    And the log should:
      """
      lines::should[]: [...
        {
          contains: 'FINE: Received worker<1> event: {"type":"io.cucumber.plugin.event.TestStepFinished"'
          contains: '"status":"AMBIGUOUS"'
        }
        { contains: 'FINE: Forwarding event: io.cucumber.plugin.event.TestStepFinished' }
        {
          contains: 'FINE: Received worker<1> event: {"type":"io.cucumber.plugin.event.TestCaseFinished"'
          contains: '"status":"AMBIGUOUS"'
        }
        { contains: 'FINE: Forwarding event: io.cucumber.plugin.event.TestCaseFinished' }
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

  Scenario: run with feature file name
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
      $path + 'features/test.feature'
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

  Scenario: raise error when no worker after time out
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
      '--worker-timeout'
      '2'
      '--disable-local-worker'
      '--glue'
      'steps'
      $path + 'features'
      """
    Then the task result should be:
      """
      : {
        code= 1
        stderr.normalize::should.startsWith: ```
                                             Exception in thread "main" java.lang.IllegalStateException: No worker available after waiting for 2 seconds
                                             ```
      }
      """
    And the log should:
      """
      lines: [...
        'INFO: No worker available after waiting for 2 seconds',
        'INFO: Shutting down master...'
        'INFO: Shutting down restful server...'
        'INFO: Restful server shut down'
        'INFO: Master shut down'
      ...]
      """
