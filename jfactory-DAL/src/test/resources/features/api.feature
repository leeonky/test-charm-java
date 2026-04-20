Feature: JFactory-DAL API

  Background:
    Given the following class definition:
      """
      public class Bean {
        public String str1, str2;
      }
      """
    Given the following spec definition:
      """
      public class BeanSpec extends Spec<Bean> {}
      """
    Given the following declarations:
      """
      JFactory jFactory = new JFactory();
      """
    Given the following declarations:
      """
      JFactoryDAL jd = JFactoryDAL.instance(jFactory);
      """
    And register as follows:
      """
      jFactory.register(BeanSpec.class);
      """

  Scenario: create(Class<T> clazz, String expression)
    When evaluating the following code:
      """
      jd.create(Bean.class, "str1: hello\nstr2: world");
      """
    Then the result should be:
      """
      = {
        str1= hello
        str2= world
        class.simpleName= Bean
      }
      """

  Scenario: collection create(String traitsSpec, String expression)
    When evaluating the following code:
      """
      jd.create("BeanSpec", "[{str1: hello, str2: world}, {str1: hello2, str2: world2}]");
      """
    Then the result should be:
      """
      = | str1   | str2   | class.simpleName |
        | hello  | world  | Bean             |
        | hello2 | world2 | Bean             |
      """

  Scenario: single create(String traitsSpec, String expression)
    When evaluating the following code:
      """
      jd.create("BeanSpec", "str1: hello\nstr2: world");
      """
    Then the result should be:
      """
      = {
        str1= hello
        str2= world
        class.simpleName= Bean
      }
      """
