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
        public void clear() { }

        @Override
        public void save(Object object) { }
      });
      """
    And the following bean definition:
      """
      public class Bean { public String str; }
      """

  @import(java.util.*)
  Scenario: Query from Repo - Query Bean from Repository
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

  @import(java.util.*)
  Scenario: Query With Criteria - Query Bean from Repository with Criteria
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

#  Scenario: Create and Query - Query Previous created Bean