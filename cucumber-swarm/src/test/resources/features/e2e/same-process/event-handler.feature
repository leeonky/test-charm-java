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
          ::this.testStep= ::this.testCase.testSteps[0]

          testCase: {
            location: { line: 2, column: 3 }
            uri: 'file://$path$/features/test.feature'
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
          ::this.testStep= ::this.testCase.testSteps[0]

          testCase: {
            location: { line: 2, column: 3 }
            uri: 'file://$path$/features/test.feature'
          }
        }{
          testStep: {
            class.name= io.cucumber.core.runner.PickleStepTestStep
            uri: 'file://$path$/features/test.feature'
            step.location: { line: 3, column: 5 }
          }
          ::this.testStep= ::this.testCase.testSteps[1]

          testCase: {
            location: { line: 2, column: 3 }
            uri: 'file://$path$/features/test.feature'
          }
        }]
        """

  Rule: test step finished

    Scenario: forward test step PickleStepTestStep finished passed
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
        [io.cucumber.plugin.event.TestStepFinished]: [{
          instant: {...}
          testStep: {
            class.name= io.cucumber.core.runner.PickleStepTestStep
            uri: 'file://$path$/features/test.feature'
            step.location: { line: 3, column: 5 }
          }
          ::this.testStep= ::this.testCase.testSteps[0]

          testCase: {
            location: { line: 2, column: 3 }
            uri: 'file://$path$/features/test.feature'
          }
          result= {
            status: PASSED
            duration: {...}
            error: null
          }
        }]
        """

    Scenario: forward test step PickleStepTestStep finished failed with serialized exception
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
              throw new RuntimeException("step failed");
          }
        }
        """
      Then the following event should be emitted after cucumber run:
        """
        [io.cucumber.plugin.event.TestStepFinished]: [{
          instant: {...}
          testStep: {
            class.name= io.cucumber.core.runner.PickleStepTestStep
            uri: 'file://$path$/features/test.feature'
            step.location: { line: 3, column: 5 }
          }
          ::this.testStep= ::this.testCase.testSteps[0]

          testCase: {
            location: { line: 2, column: 3 }
            uri: 'file://$path$/features/test.feature'
          }
          result= {
            status: FAILED
            duration: {...}
            error: {
              class.simpleName= RuntimeException
              message= "step failed"
              stackTrace.fileName[]= [Steps.java ...]
            }
          }
        }]
        """

    Scenario: forward test step PickleStepTestStep finished skipped
      Given the feature file "test.feature":
        """
        Feature: test
          Scenario: test
            Given a step with implementation
            Given another step with implementation
        """
      Given the following class definition:
        """
        package steps;
        import io.cucumber.java.en.*;

        public class Steps {

          @Given("a step with implementation")
          public void a_step_with_implementation() {
              throw new RuntimeException("step failed");
          }

          @Given("another step with implementation")
          public void another_step_with_implementation() {
          }
        }
        """
      Then the following event should be emitted after cucumber run:
        """
        [io.cucumber.plugin.event.TestStepFinished]: [{
          testStep: {
            step.location: { line: 3, column: 5 }
          }
          ::this.testStep= ::this.testCase.testSteps[0]

          result.status: FAILED
        } {
          testStep: {
            class.name= io.cucumber.core.runner.PickleStepTestStep
            uri: 'file://$path$/features/test.feature'
            step.location: { line: 4, column: 5 }
          }
          ::this.testStep= ::this.testCase.testSteps[1]

          result= {
            status: SKIPPED
            duration: {...}
            error: null
          }
        }]
        """

    Scenario: forward test step PickleStepTestStep finished pending
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
            throw new io.cucumber.java.PendingException("not implemented yet");
          }
        }
        """
      Then the following event should be emitted after cucumber run:
        """
        [io.cucumber.plugin.event.TestStepFinished]: [{
          testStep: {
            class.name= io.cucumber.core.runner.PickleStepTestStep
            uri: 'file://$path$/features/test.feature'
            step.location: { line: 3, column: 5 }
          }
          ::this.testStep= ::this.testCase.testSteps[0]

          result= {
            status: PENDING
            duration: {...}
            error.class.simpleName= PendingException
          }
        }]
        """

    Scenario: forward test step PickleStepTestStep finished undefined
      Given the feature file "test.feature":
        """
        Feature: test
          Scenario: test
            Given a step with implementation
        """
      Then the following event should be emitted after cucumber run:
        """
        [io.cucumber.plugin.event.TestStepFinished]: [{
          testStep: {
            class.name= io.cucumber.core.runner.PickleStepTestStep
            uri: 'file://$path$/features/test.feature'
            step.location: { line: 3, column: 5 }
          }
          ::this.testStep= ::this.testCase.testSteps[0]

          result= {
            status: UNDEFINED
            duration: {...}
            error: null
          }
        }]
        """

    Scenario: forward test step PickleStepTestStep finished ambiguous
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
      Then the following event should be emitted after cucumber run:
        """
        [io.cucumber.plugin.event.TestStepFinished]: [{
          testStep: {
            class.name= io.cucumber.core.runner.PickleStepTestStep
            uri: 'file://$path$/features/test.feature'
            step.location: { line: 3, column: 5 }
          }
          ::this.testStep= ::this.testCase.testSteps[0]

          testCase: {
            location: { line: 2, column: 3 }
            uri: 'file://$path$/features/test.feature'
          }
          result= {
            status: AMBIGUOUS
            duration: {...}
            error.message= ```
                           "a step with implementation" matches more than one step definition:
                             "^.* implementation$" in steps.Steps.another_a_step_with_implementation()
                             "a step with implementation" in steps.Steps.a_step_with_implementation()
                           ```
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
