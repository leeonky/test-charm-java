Feature: event serializer

  Rule: test case started

    Scenario: forward test case started
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
          }
        }]
        """
