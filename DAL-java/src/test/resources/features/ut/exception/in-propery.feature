Feature: exception in property

#  Scenario: not exist property
#    Given the following java class:
#      """
#      public class Data {
#        public int value() {
#          throw new java.lang.RuntimeException("Error");
#        }
#      }
#      """
#    When use a instance of java class "Data" to assert:
#      """
#      valueX: any
#      """
#    Then got assert error:
#      """
#      message= ```
#               value: any
#               ^
#
#               Get property `value` failed, property can be:
#                 1. public field
#                 2. public getter
#                 3. public method
#                 4. Map key value
#                 5. customized type getter
#                 6. static method extension
#               Method or property `notExist` does not exist in `src.test.generate.ws0.Data`
#
#               The root value was: #package#Data {}
#               ```
#      """