Feature: Data Repository

  @import(java.util.*)
  Scenario: Save To Repo - Save Bean to Repository After Bean Created
    Given the following declarations:
      """
      List<Object> created = new ArrayList<>();
      """
    And the following declarations:
      """
      JFactory jFactory = new JFactory(new DataRepository() {
        @Override
        public <T> Collection<T> queryAll(Class<T> type) {
            return Collections.emptyList();
        }

        @Override
        public void clear() { }

        @Override
        public void save(Object object) { created.add(object); }
      });
      """
    And the following bean definition:
      """
      public class Bean { public String str; }
      """
    When execute as follows:
      """
      jFactory.type(Bean.class).property("str", "hello").create();
      """
    Then value of "created" should be:
      """
      = [{ class.simpleName= Bean, str= hello }]
      """
    And execute as follows:
      """
      jFactory.type(Bean.class).property("str", "world").create();
      """
    Then value of "created" should be:
      """
      = [{ class.simpleName= Bean, str= hello }, { class.simpleName= Bean, str= world }]
      """

  @import(java.util.*)
  Scenario: Query from Repo - Query Bean from Repository
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
    And execute as follows:
      """
      Bean bean = new Bean();
      bean.str = "hello";
      created.add(bean);
      """
    When evaluating the following code:
      """
      jFactory.type(Bean.class).queryAll();
      """
    Then the result should be:
      """
      = [{ class.simpleName= Bean, str= hello }]
      """
