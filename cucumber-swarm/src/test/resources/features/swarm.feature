Feature: swarm

  Scenario: cucumber launcher and verify stdout
    When run cucumber with the following args:
      """
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

  Scenario: given step without step and verify error
    Given the feature file "no-step.feature":
      """
      Feature: no step

        Scenario: no step
          Given a step without implementation
      """
    When run cucumber with the following args:
      """
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
                          5 steps (4 passed, 1 undefined)

                          You can implement missing steps with the snippets below:

                          @Given("a step without implementation")
                          public void a_step_without_implementation() {
                              // Write code here that turns the phrase above into concrete actions
                              throw new io.cucumber.java.PendingException();
                          }
                          ```
      }
      """
