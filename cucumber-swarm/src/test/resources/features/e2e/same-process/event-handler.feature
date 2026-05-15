Feature: event serializer

  Rule: test case started

    Scenario: forward test case started with no step
      Given the feature file "test.feature":
        """
        Feature: test
          Scenario: test
        """
      Then the following event should be emitted after cucumber run:
        """
        [io.cucumber.plugin.event.TestCaseStarted]: [{
          instant: {...}
          testCase: {
            location: { line: 2, column: 3 }
            uri: 'file://$path$/features/test.feature'
            testSteps: []
          }
        }]
        """

    Scenario: forward test case started with one PickleStepTestStep
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
        [io.cucumber.plugin.event.TestCaseStarted]: [{
          instant: {...}
          testCase: {
            location: { line: 2, column: 3 }
            uri: 'file://$path$/features/test.feature'
            testSteps: [{
              class.name= io.cucumber.core.runner.PickleStepTestStep
              uri: 'file://$path$/features/test.feature'
              step.location: { line: 3, column: 5 }
            }]
          }
        }]
        """

    Scenario: forward test case started with hook step
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

          @io.cucumber.java.Before
          public void before() {}
        }
        """
      Then the following event should be emitted after cucumber run:
        """
        [io.cucumber.plugin.event.TestCaseStarted]: [{
          instant: {...}
          testCase: {
            location: { line: 2, column: 3 }
            uri: 'file://$path$/features/test.feature'
            testSteps: [{
              class.name= io.cucumber.core.runner.HookTestStep
              hookType: BEFORE
            }{
              class.name= io.cucumber.core.runner.PickleStepTestStep
              uri: 'file://$path$/features/test.feature'
              step.location: { line: 3, column: 5 }
            }]
          }
        }]
        """

  Rule: test step started

    Scenario: forward test step PickleStepTestStep started
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
        [io.cucumber.plugin.event.TestStepStarted]: [{
          testStep: {
            class.name= io.cucumber.core.runner.PickleStepTestStep
            uri: 'file://$path$/features/test.feature'
            step.location: { line: 3, column: 5 }
          }
          testCase: {
            location: { line: 2, column: 3 }
            uri: 'file://$path$/features/test.feature'
            testSteps[0]= ::root[io.cucumber.plugin.event.TestStepStarted][0].testStep
          }
        }]
        """

    Scenario: forward test step HookTestStep and PickleStepTestStep started
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

          @io.cucumber.java.Before
          public void before() {}

          @Given("a step with implementation")
          public void a_step_with_implementation() {
              System.out.println("step called");
          }
        }
        """
      Then the following event should be emitted after cucumber run:
        """
        [io.cucumber.plugin.event.TestStepStarted]: [{
          testStep: {
            class.name= io.cucumber.core.runner.HookTestStep
            hookType: BEFORE
          }
          testCase: {
            location: { line: 2, column: 3 }
            uri: 'file://$path$/features/test.feature'
            testSteps[0]= ::root[io.cucumber.plugin.event.TestStepStarted][0].testStep
          }
        }{
          testStep: {
            class.name= io.cucumber.core.runner.PickleStepTestStep
            uri: 'file://$path$/features/test.feature'
            step.location: { line: 3, column: 5 }
          }
          testCase: {
            location: { line: 2, column: 3 }
            uri: 'file://$path$/features/test.feature'
            testSteps[1]= ::root[io.cucumber.plugin.event.TestStepStarted][1].testStep
          }
        }]
        """

  Rule: test case finished

    Scenario: forward test case finished passed and no step
      Given the feature file "test.feature":
        """
        Feature: test
          Scenario: test
        """
      Then the following event should be emitted after cucumber run:
        """
        [io.cucumber.plugin.event.TestCaseFinished]: [{
          instant: {...}
          testCase: {
            location: { line: 2, column: 3 }
            uri: 'file://$path$/features/test.feature'
            testSteps: []
          }
          result= {
            status: PASSED
            duration: {...}
            error: null
          }
        }]
        """

    Scenario: forward test case finished passed and one PickleStepTestStep
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
        [io.cucumber.plugin.event.TestCaseFinished]: [{
          instant: {...}
          testCase: {
            location: { line: 2, column: 3 }
            uri: 'file://$path$/features/test.feature'
            testSteps: [{
              class.name= io.cucumber.core.runner.PickleStepTestStep
              uri: 'file://$path$/features/test.feature'
              step.location: { line: 3, column: 5 }
            }]
          }
          result= {
            status: PASSED
            duration: {...}
            error: null
          }
        }]
        """

    Scenario: forward test case finished passed and hook step
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

          @io.cucumber.java.Before
          public void before() {}

          @Given("a step with implementation")
          public void a_step_with_implementation() {
              System.out.println("step called");
          }
        }
        """
      Then the following event should be emitted after cucumber run:
        """
        [io.cucumber.plugin.event.TestCaseFinished]: [{
          instant: {...}
          testCase: {
            location: { line: 2, column: 3 }
            uri: 'file://$path$/features/test.feature'
            testSteps: [{
              class.name= io.cucumber.core.runner.HookTestStep
              hookType: BEFORE
            }{
            class.name= io.cucumber.core.runner.PickleStepTestStep
              uri: 'file://$path$/features/test.feature'
              step.location: { line: 3, column: 5 }
            }]
          }
          result= {
            status: PASSED
            duration: {...}
            error: null
          }
        }]
        """
