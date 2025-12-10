Feature: Save Bean to Repository

  Scenario: Default Save - Save Bean to the Default Memory Repository After Bean Created
    Given the following declarations:
      """
      JFactory jFactory = new JFactory();
      """
    And the following bean definition:
      """
      public class Bean { public String str; }
      """
    When execute as follows:
      """
      jFactory.type(Bean.class).property("str", "hello").create();
      """
    Then the field "jFactory" should be:
      """
      dataRepository.allData::entries: ^| {simpleName: Bean} | [{str= hello}] |
      """

  @import(java.util.*)
  Scenario: Custom Repository - Define Custom Repository to Save
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
    Then the field "created" should be:
      """
      = [{ class.simpleName= Bean, str= hello }]
      """
    And execute as follows:
      """
      jFactory.type(Bean.class).property("str", "world").create();
      """
    Then the field "created" should be:
      """
      = [{ class.simpleName= Bean, str= hello }, { class.simpleName= Bean, str= world }]
      """
