Feature: Master and worker

  Rule: Master should not invoke beforeAll and afterAll hooks

    Background:
      Given the following class definition:
        """
        package steps;
        import io.cucumber.java.en.*;
        import io.cucumber.java.*;

        public class Steps {

          @BeforeAll
          public static void before_all() {
            System.out.println("before-all");
          }

          @AfterAll
          public static void after_all() {
            System.out.println("after-all");
          }
        }
        """

    Scenario: empty features
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
                            before-all
                            after-all

                            0 scenarios
                            0 steps
                            ```
        }
        """
