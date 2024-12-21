Feature: async

  Rule: eventually

    Scenario: object eventually pass
      Given the following java class:
      """
      public class Data {
        private Instant time = Instant.now();

        public int getInt() {
          if(Instant.now().getEpochSecond() - time.getEpochSecond() >= 1 )
            return 100;
           return -1;
        }
      }
      """
      Then the following should pass:
      """
      ::eventually: {
        int: 100
      }
      """

    Scenario: support set global max waiting time in java code
      Given the following java class:
      """
      public class Data {
        public Data() {
            Eventually.setDefaultWaitingTime(100);
        }

        private Instant time = Instant.now();

        public int getInt() {
          if(Instant.now().getEpochSecond() - time.getEpochSecond() >= 1 )
            return 100;
           return -1;
        }
      }
      """
      When evaluate by:
      """
      ::eventually: {
        int: 100
      }
      """
      Then failed with the message:
      """

      ::eventually: {
        int: 100
             ^
      }

      Expected to match: java.lang.Integer
      <100>
       ^
      Actual: java.lang.Integer
      <-1>
       ^

      The root value was: #package#Data {
          int: java.lang.Integer <-1>
      }
      """

    Scenario: support set waiting time in dal
      Given the following java class:
      """
      public class Data {
        private Instant time = Instant.now();

        public int getInt() {
          if(Instant.now().getEpochSecond() - time.getEpochSecond() >= 2 )
            return 100;
           return -1;
        }
      }
      """
      When evaluate by:
      """
      ::eventually.within(1s): {
        int: 100
      }
      """
      Then failed with the message:
      """

      ::eventually.within(1s): {
        int: 100
             ^
      }

      Expected to match: java.lang.Integer
      <100>
       ^
      Actual: java.lang.Integer
      <-1>
       ^

      The root value was: #package#Data {
          int: java.lang.Integer <-1>
      }
      """

    Scenario: support set interval time in dal
      Given the following java class:
      """
      public class Data {
        private Instant time = Instant.now();

        public int getInt() {
          if(Instant.now().getEpochSecond() - time.getEpochSecond() >= 1 )
            return 100;
           return -1;
        }
      }
      """
      When evaluate by:
      """
      ::eventually.within(1.5s).interval(2s): {
        int: 100
      }
      """
      Then failed with the message:
      """

      ::eventually.within(1.5s).interval(2s): {
        int: 100
             ^
      }

      Expected to match: java.lang.Integer
      <100>
       ^
      Actual: java.lang.Integer
      <-1>
       ^

      The root value was: #package#Data {
          int: java.lang.Integer <-1>
      }
      """

  Rule: await

    Scenario: await return result ignore exception it default 5 seconds
      Given the following java class:
      """
      public class DataList {
        public static class Data {
          public int i,j;
        }
        private Instant time = Instant.now();

        public List<Data> getList() {
          if(Instant.now().getEpochSecond() - time.getEpochSecond() >= 1) {
            Data d1 = new Data();
            d1.i = 1;
            d1.j = 100;
            Data d2 = new Data();
            d2.i = 2;
            d2.j = 200;
            return Arrays.asList(d1, d2);
          }
          return Arrays.asList();
        }
      }
      """
      Then the following should pass:
      """
      ::await: {list::filter!: {i=1}}
      : [{
        i= 1
        j= 100
      }]
      """

    Scenario: set await max waiting time in java
      Given the following java class:
      """
      public class DataList {
        public DataList() {
            Await.setDefaultWaitingTime(1000);
        }

        public static class Data {
          public int i,j;
        }
        private Instant time = Instant.now();

        public List<Data> getList() {
          if(Instant.now().getEpochSecond() - time.getEpochSecond() >= 2) {
            Data d1 = new Data();
            d1.i = 1;
            d1.j = 100;
            Data d2 = new Data();
            d2.i = 2;
            d2.j = 200;
            return Arrays.asList(d1, d2);
          }
          return Arrays.asList();
        }
      }
      """
      When evaluate by:
      """
      ::await: {list::filter!: {i=1}}
      """
      Then failed with the message:
      """

      ::await: {list::filter!: {i=1}}
                            ^

      Filtered result is empty, try again

      The root value was: #package#DataList {
          list: []
      }
      """

    Scenario: set current await max waiting time in dal
      Given the following java class:
      """
      public class DataList {
        public static class Data {
          public int i,j;
        }
        private Instant time = Instant.now();

        public List<Data> getList() {
          if(Instant.now().getEpochSecond() - time.getEpochSecond() >= 2) {
            Data d1 = new Data();
            d1.i = 1;
            d1.j = 100;
            Data d2 = new Data();
            d2.i = 2;
            d2.j = 200;
            return Arrays.asList(d1, d2);
          }
          return Arrays.asList();
        }
      }
      """
      When evaluate by:
      """
      ::await(1s): {list::filter!: {i=1}}
      """
      Then failed with the message:
      """

      ::await(1s): {list::filter!: {i=1}}
                                ^

      Filtered result is empty, try again

      The root value was: #package#DataList {
          list: []
      }
      """

    Scenario: set interval in DAL
      Given the following java class:
      """
      public class DataList {
        public static class Data {
          public int i,j;
        }
        private Instant time = Instant.now();

        public List<Data> getList() {
          if(Instant.now().getEpochSecond() - time.getEpochSecond() >= 1) {
            Data d1 = new Data();
            d1.i = 1;
            d1.j = 100;
            Data d2 = new Data();
            d2.i = 2;
            d2.j = 200;
            return Arrays.asList(d1, d2);
          }
          return Arrays.asList();
        }
      }
      """
      When evaluate by:
      """
      ::await(1.5s).interval(2s): {list::filter!: {i=1}}
      """
      Then failed with the message:
      """

      ::await(1.5s).interval(2s): {list::filter!: {i=1}}
                                               ^

      Filtered result is empty, try again

      The root value was: #package#DataList {
          list: []
      }
      """
