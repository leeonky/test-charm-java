Feature: event serializer

  Rule: test case started

    Scenario: deserialize test case from master mapped test case
      Given master side "EventTestCase":
        """
        location: 100
        uri: '/master/features/test.feature'
        """
      When serialize the following "EventTestCaseStarted" in executor side and deserialize in master side:
        """
        instant: '1970-01-01T00:00:01Z'
        testCase: {
          location: 100
          uri: '/executor/features/test.feature'
        }
        """
      Then the deserialized object should be:
        """
        : {
          class.name: io.cucumber.plugin.event.TestCaseStarted
          instant: '1970-01-01T00:00:01Z'
          testCase: {
            location: { line: 100, column: 0 }
            uri: 'file:///master/features/test.feature'
          }
        }
        """

  Rule: test case finished

    Scenario: deserialize test case from master mapped test case
      Given master side "EventTestCase":
        """
        location: 100
        uri: '/master/features/test.feature'
        """
      When serialize the following "EventTestCaseFinished" in executor side and deserialize in master side:
        """
        instant: '1970-01-01T00:00:01Z'
        result: {
          status: 'PASSED'
          duration: 100
        }
        testCase: {
          location: 100
          uri: '/executor/features/test.feature'
        }
        """
      Then the deserialized object should be:
        """
        : {
          class.name: io.cucumber.plugin.event.TestCaseFinished
          instant: '1970-01-01T00:00:01Z'
          result: {
            status: 'PASSED'
            duration.toMillis: 100
          }
          testCase: {
            location: { line: 100, column: 0 }
            uri: 'file:///master/features/test.feature'
          }
        }
        """
