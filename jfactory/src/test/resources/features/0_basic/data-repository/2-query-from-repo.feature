@import(java.util.*)
Feature: Data Repository

  Background:
    Given the following declarations:
      """
      List<Object> created = new ArrayList<>();
      """
    And the following declarations:
      """
      JFactory jFactory = new JFactory(new DataRepository() {
        @Override
        public <T> Collection<T> queryAll(Class<T> type) {
            return (Collection<T>)created;
        }

        @Override
        public void clear() { created.clear(); }

        @Override
        public void save(Object object) { }
      });
      """
    And the following bean definition:
      """
      public class Bean { public String str; }
      """

  Scenario: Query from Repo - Query All Object from Repository
    When execute as follows:
      """
      Bean bean = new Bean();
      bean.str = "hello";
      created.add(bean);
      """
    And evaluating the following code:
      """
      jFactory.type(Bean.class).queryAll();
      """
    Then the result should be:
      """
      = [{ class.simpleName= Bean, str= hello }]
      """

  Scenario: Query With Criteria - Query All Object from Repository with Criteria
    When execute as follows:
      """
      Bean bean1 = new Bean();
      bean1.str = "hello";
      created.add(bean1);

      Bean bean2 = new Bean();
      bean2.str = "world";
      created.add(bean2);
      """
    And evaluating the following code:
      """
      jFactory.type(Bean.class).property("str", "world").queryAll()
      """
    Then the result should be:
      """
      = [{ class.simpleName= Bean, str= world }]
      """

  Scenario: Query Single - Query Single Object from Repository with Criteria
    When execute as follows:
      """
      Bean bean1 = new Bean();
      bean1.str = "hello";
      created.add(bean1);

      Bean bean2 = new Bean();
      bean2.str = "world";
      created.add(bean2);
      """
    And evaluating the following code:
      """
      jFactory.type(Bean.class).property("str", "world").query()
      """
    Then the result should be:
      """
      = { class.simpleName= Bean, str= world }
      """

  Scenario: Query Single None - Return Null When No Object Found
    When execute as follows:
      """
      Bean bean = new Bean();
      bean.str = "hello";
      created.add(bean);
      """
    And evaluating the following code:
      """
      jFactory.type(Bean.class).property("str", "not-exist").query()
      """
    Then the result should be:
      """
      = null
      """

  Scenario: Query Single Strict - Raise Error When Multiple Objects Returned
    When execute as follows:
      """
      Bean bean1 = new Bean();
      bean1.str = "hello";
      created.add(bean1);

      Bean bean2 = new Bean();
      bean2.str = "hello";
      created.add(bean2);
      """
    And evaluating the following code:
      """
      jFactory.type(Bean.class).property("str", "hello").query()
      """
    Then the result should be:
      """
      ::throw.message= 'There are multiple elements in the query result.'
      """

  Scenario: Clear Repo - Clear Repository Data
    When execute as follows:
      """
      Bean bean1 = new Bean();
      bean1.str = "hello";
      created.add(bean1);

      Bean bean2 = new Bean();
      bean2.str = "world";
      created.add(bean2);
      """
    And execute as follows:
      """
      jFactory.getDataRepository().clear();
      """
    And evaluating the following code:
      """
      jFactory.type(Bean.class).queryAll()
      """
    Then the result should be:
      """
      = []
      """

#  Scenario: Create and Query - Query Previous created Bean