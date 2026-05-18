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
              hookId.present: false
              id: ::DB.testCases[0].testSteps[0].id
              pickleStepId.get: ::DB.pickles[0].steps[0].id
              stepDefinitionIds.get: [::DB.stepDefinitions[0].id]
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

    Scenario: forward test case with PickleStepTestStep and args
      Given the feature file "test.feature":
        """
        Feature: test
          Scenario: test
            Given a step with number 123
        """
      Given the following class definition:
        """
        package steps;
        import io.cucumber.java.en.*;

        public class Steps {

          @Given("a step with number {int}")
          public void a_step_with_number(int number) {
            System.out.println(number);
          }
        }
        """
      Then the following event should be emitted after cucumber run:
        """
        [io.cucumber.messages.types.Envelope]::filter: {testCase.present: true}: [{
          testCase.get: {
            testSteps: [{
              stepDefinitionIds.get: [::DB.stepDefinitions[0].id]
              stepMatchArgumentsLists.get: [{
                stepMatchArguments: [{
                  group: {
                    value.get: '123'
                    start.get: 19
                    children: []
                  }
                  parameterTypeName.get: 'int'
                }]
              }]
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

    Scenario: forward test case with PickleStepTestStep and nested args
      Given the feature file "test.feature":
        """
        Feature: test
          Scenario: test
            Given I have 100 apples
        """
      Given the following class definition:
        """
        package steps;
        import io.cucumber.java.en.*;
        import io.cucumber.datatable.DataTable;

        public class Steps {

          @Given("^I have ((\\d+)( apples))$")
          public void i_have_groups(String outer, String digits, String apples) {
          }
        }
        """
      Then the following event should be emitted after cucumber run:
        """
        [io.cucumber.messages.types.Envelope]::filter: {testCase.present: true}: [{
          testCase.get: {
            testSteps: [{
            stepDefinitionIds.get: [::DB.stepDefinitions[0].id]
              stepMatchArgumentsLists.get: [{
                stepMatchArguments: [{
                  parameterTypeName.get: anonymous
                  group: {
                    start.get: 7
                    children: [
                      {
                        start.get: 7,
                        children: [],
                        value.get: '100'
                      },
                      {
                        start.get: 10,
                        children: [],
                        value.get: ' apples'
                      }
                    ],
                    value.get: '100 apples'
                  }
                }]
              }]
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

    Scenario: forward test case with HookTestStep and PickleStepTestStep
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
        import io.cucumber.java.*;

        public class Steps {

          @Before
          public void before() {}

          @Given("a step with implementation")
          public void a_step_with_implementation() {}
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
              pickleStepId.present: false
              hookId.get: ::DB.hooks[0].id
              stepDefinitionIds.get: []
            },
            {
              id: ::DB.testCases[0].testSteps[1].id
              pickleStepId.get: ::DB.pickles[0].steps[0].id
              stepDefinitionIds.get: [::DB.stepDefinitions[0].id]
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

  Rule: test case started

    Scenario: forward test case started
      Given the feature file "test.feature":
          """
          Feature: test
          Scenario: test
          """
      Then the following event should be emitted after cucumber run:
          """
          [io.cucumber.messages.types.Envelope]::filter: {testCaseStarted.present: true}: [{
            testCaseStarted.get: {
              attempt: {...}
              id: {...}
              testCaseId: ::DB.testCases[0].id
              workerId.get: {...}
              timestamp: {...}
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

  Rule: test step started

    Scenario: forward test step started with one PickleStepTestStep
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
        [io.cucumber.messages.types.Envelope]::filter: {testStepStarted.present: true}: [{
          testStepStarted.get: {
            testCaseStartedId: (::root[io.cucumber.messages.types.Envelope]::filter: {testCaseStarted.present: true}).testCaseStarted.get.id
            testStepId: ::DB.testCases[0].testSteps[0].id
            timestamp: {...}
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

    Scenario: forward test step started with HookTestStep and PickleStepTestStep
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
        import io.cucumber.java.*;

        public class Steps {

          @Before
          public void before() {}

          @Given("a step with implementation")
          public void a_step_with_implementation() {}
        }
        """
      Then the following event should be emitted after cucumber run:
        """
        [io.cucumber.messages.types.Envelope]::filter: {testStepStarted.present: true}: [{
          testStepStarted.get: {
            testCaseStartedId: (::root[io.cucumber.messages.types.Envelope]::filter: {testCaseStarted.present: true}).testCaseStarted.get.id
            testStepId: ::DB.testCases[0].testSteps[0].id
            timestamp: {...}
          }
        }, {
          testStepStarted.get: {
            testCaseStartedId: (::root[io.cucumber.messages.types.Envelope]::filter: {testCaseStarted.present: true}).testCaseStarted.get.id
            testStepId: ::DB.testCases[0].testSteps[1].id
            timestamp: {...}
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

  Rule: test step finished

    Scenario: forward test step finished passed
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
        [io.cucumber.messages.types.Envelope]::filter: {testStepFinished.present: true}: [{
          testStepFinished.get: {
            testCaseStartedId: (::root[io.cucumber.messages.types.Envelope]::filter: {testCaseStarted.present: true}).testCaseStarted.get.id
            testStepId: ::DB.testCases[0].testSteps[0].id
            testStepResult: {
              status: 'PASSED'
              duration: {...}
              exception.present: false
              message.present: false
            }
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

    Scenario: forward test step finished failed
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
        [io.cucumber.messages.types.Envelope]::filter: {testStepFinished.present: true}: [{
          testStepFinished.get: {
            testCaseStartedId: (::root[io.cucumber.messages.types.Envelope]::filter: {testCaseStarted.present: true}).testCaseStarted.get.id
            testStepId: ::DB.testCases[0].testSteps[0].id
            testStepResult: {
              status: 'FAILED'
              duration: {...}
              message.get: 'step failed'
              exception.get: {
                message.get: 'step failed'
                stackTrace.get: ```
                                java.lang.RuntimeException: step failed
                                \tat steps.Steps.a_step_with_implementation(Steps.java:9)
                                \tat ✽.a step with implementation(file:///opt/share/test-charm/test-charm-java/cucumber-swarm/src/test/generate/t0/cucumber/features/test.feature:3)

                                ```
                type: 'java.lang.RuntimeException'
              }
            }
            timestamp: {...}
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

    Scenario: forward test step finished skipped
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
        [io.cucumber.messages.types.Envelope]::filter: {testStepFinished.present: true}: [{
          testStepFinished.get: {
            testCaseStartedId: (::root[io.cucumber.messages.types.Envelope]::filter: {testCaseStarted.present: true}).testCaseStarted.get.id
            testStepId: ::DB.testCases[0].testSteps[0].id
            testStepResult: {
              status: 'FAILED'
              duration: {...}
              message.get: 'step failed'
              exception.get: {
                message.get: 'step failed'
                stackTrace.get: ```
                                java.lang.RuntimeException: step failed
                                \tat steps.Steps.a_step_with_implementation(Steps.java:9)
                                \tat ✽.a step with implementation(file:///opt/share/test-charm/test-charm-java/cucumber-swarm/src/test/generate/t0/cucumber/features/test.feature:3)

                                ```
                type: 'java.lang.RuntimeException'
              }
            }
            timestamp: {...}
          }
        }, {
          testStepFinished.get: {
            testCaseStartedId: (::root[io.cucumber.messages.types.Envelope]::filter: {testCaseStarted.present: true}).testCaseStarted.get.id
            testStepId: ::DB.testCases[0].testSteps[1].id
            testStepResult: {
              status: 'SKIPPED'
              duration: {...}
              message.present: false
              exception.present: false
            }
            timestamp: {...}
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

    Scenario: forward test step finished pending
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
              throw new io.cucumber.java.PendingException("step pending");
          }
        }
        """
      Then the following event should be emitted after cucumber run:
        """
        [io.cucumber.messages.types.Envelope]::filter: {testStepFinished.present: true}: [{
          testStepFinished.get: {
            testCaseStartedId: (::root[io.cucumber.messages.types.Envelope]::filter: {testCaseStarted.present: true}).testCaseStarted.get.id
            testStepId: ::DB.testCases[0].testSteps[0].id
            testStepResult: {
              status: 'PENDING'
              duration: {...}
              message.get: 'step pending'
              exception.get: {
                message.get: 'step pending'
                stackTrace.get: ```
                                io.cucumber.java.PendingException: step pending
                                \tat steps.Steps.a_step_with_implementation(Steps.java:9)
                                \tat ✽.a step with implementation(file:///opt/share/test-charm/test-charm-java/cucumber-swarm/src/test/generate/t0/cucumber/features/test.feature:3)

                                ```
                type: 'io.cucumber.java.PendingException'
              }
            }
            timestamp: {...}
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

    Scenario: forward test step finished undefined
      Given the feature file "test.feature":
        """
        Feature: test
          Scenario: test
            Given a step with implementation
        """
      Then the following event should be emitted after cucumber run:
        """
        [io.cucumber.messages.types.Envelope]::filter: {testStepFinished.present: true}: [{
          testStepFinished.get: {
            testCaseStartedId: (::root[io.cucumber.messages.types.Envelope]::filter: {testCaseStarted.present: true}).testCaseStarted.get.id
            testStepId: ::DB.testCases[0].testSteps[0].id
            testStepResult: {
              status: 'UNDEFINED'
              duration: {...}
              message.present: false
              exception.present: false
            }
            timestamp: {...}
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

    Scenario: forward test step finished ambiguous
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
        [io.cucumber.messages.types.Envelope]::filter: {testStepFinished.present: true}: [{
          testStepFinished.get: {
            testCaseStartedId: (::root[io.cucumber.messages.types.Envelope]::filter: {testCaseStarted.present: true}).testCaseStarted.get.id
            testStepId: ::DB.testCases[0].testSteps[0].id
            testStepResult: {
              status: 'AMBIGUOUS'
              duration: {...}
              message.get: ```
                           "a step with implementation" matches more than one step definition:
                             "^.* implementation$" in steps.Steps.another_a_step_with_implementation()
                             "a step with implementation" in steps.Steps.a_step_with_implementation()
                           ```
              exception.get: {
                message.get: ```
                             "a step with implementation" matches more than one step definition:
                               "^.* implementation$" in steps.Steps.another_a_step_with_implementation()
                               "a step with implementation" in steps.Steps.a_step_with_implementation()
                             ```

                stackTrace.get::should.startsWith: ```
                                                   io.cucumber.core.runner.AmbiguousStepDefinitionsException: "a step with implementation" matches more than one step definition:
                                                     "^.* implementation$" in steps.Steps.another_a_step_with_implementation()
                                                     "a step with implementation" in steps.Steps.a_step_with_implementation()
                                                   ```

                type: 'io.cucumber.core.runner.AmbiguousStepDefinitionsException'
              }
            }
            timestamp: {...}
          }
        }]
        """

  Rule: test case finished

    Scenario: forward test case finished passed
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
        [io.cucumber.messages.types.Envelope]::filter: {testCaseFinished.present: true}: [{
          testCaseFinished.get: {
            testCaseStartedId: (::root[io.cucumber.messages.types.Envelope]::filter: {testCaseStarted.present: true}).testCaseStarted.get.id
            timestamp: {...}
            willBeRetried: {...}
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
