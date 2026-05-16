Feature: envelop forwarding

  Rule: test case

    Scenario: forward test case with no step
      Given the feature file "test.feature":
        """
        Feature: test
          Scenario: test
        """
      Then the following event should be emitted after cucumber run:
        """
        [io.cucumber.messages.types.Envelope]::filter: {testCase.present: true}: [{
          testCase.get: {
            id: ::DB.testCases[0].id
            pickleId: ::DB.pickles[0].id
            testSteps: []
          }
        }]
        """
      And the log should:
        """
        lines::filter: {::should.contains: 'ignore envelop forwarding'}::should[]: [
          {contains: 'testRunStarted=TestRunStarted'}
          {contains: 'testRunFinished=TestRunFinished'}
        ]
        """

    Scenario: forward test case with one PickleStepTestStep
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
      Then the following event should be emitted after cucumber run:
        """
        [io.cucumber.messages.types.Envelope]::filter: {testCase.present: true}: [{
          testCase.get: {
            id: ::DB.testCases[0].id
            pickleId: ::DB.pickles[0].id
            testSteps: [{
              id: ::DB.testCases[0].testSteps[0].id
              pickleStepId.get: ::DB.pickles[0].steps[0].id
            }]
          }
        }]
        """
      And the log should:
        """
        lines::filter: {::should.contains: 'ignore envelop forwarding'}::should[]: [
          {contains: 'testRunStarted=TestRunStarted'}
          {contains: 'testRunFinished=TestRunFinished'}
        ]
        """
