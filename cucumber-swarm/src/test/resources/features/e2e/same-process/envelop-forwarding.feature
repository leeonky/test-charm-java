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
