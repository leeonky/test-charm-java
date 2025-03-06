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
      ::eventually::in(1s): {
        int: 100
      }
      """
      Then failed with the message:
      """

      ::eventually::in(1s): {
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
        private int i = 0;

        public int getInt() {
           return i++;
        }
      }
      """
      When evaluate by:
      """
      ::eventually::in(1s)::every(0.5s): {
        int: 100
      }
      """
      Then failed with the message:
      """

      ::eventually::in(1s)::every(0.5s): {
        int: 100
             ^
      }

      Expected to match: java.lang.Integer
      <100>
       ^
      Actual: java.lang.Integer
      <2>
       ^

      The root value was: #package#Data {
          int: java.lang.Integer <3>
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
      public class Data {
        private Instant time = Instant.now();
        private int i = 0;

        public int getInt() {
           i++;
           throw new java.lang.RuntimeException();
        }
        public int getInt2() {
          return i;
        }
      }
      """
      When evaluate by:
      """
      ::await(1s)::every(0.5s).int: 100
      """
      Then failed with the message:
      """

      ::await(1s)::every(0.5s).int: 100
                               ^

      Get property `int` failed, property can be:
        1. public field
        2. public getter
        3. public no args method
        4. Map key value
        5. customized type getter
        6. static method extension
      com.github.leeonky.dal.runtime.PropertyAccessException: com.github.leeonky.util.InvocationException: java.lang.RuntimeException

      The root value was: #package#Data {
          int2: java.lang.Integer <3>,
          int: *throw* com.github.leeonky.dal.runtime.PropertyAccessException: com.github.leeonky.util.InvocationException: java.lang.RuntimeException
      }
      """

    Scenario: await single property
      Given the following java class:
      """
      public class Data {
        private Instant time = Instant.now();

        public int getInt() {
          if(Instant.now().getEpochSecond() - time.getEpochSecond() >= 1 )
            return 100;
          throw new java.lang.RuntimeException();
        }
      }
      """
      Then the following should pass:
      """
      ::await.int= 100
      """

    Scenario: default timeout is waiting time when set null to default timeout
      Given the following java class:
      """
      public class Data {
        public Data() {
          Retryer.setDefaultTimeout(0);
        }
        private int i = 1;

        public int getInt() throws Exception {
            Thread.sleep(100);
            i++;
            Thread.sleep(100);
            i++;
            return 100;
        }

        public int getInt2() {
            return i;
        }
      }
      """
      Then the following should pass:
      """
      : {
        ::await(0.1s).int::throw: {...}

        int2= 2
      }
      """

    Scenario: default timeout is waiting time when default timeout less than waiting time
      Given the following java class:
      """
      public class Data {
        public Data() {
          Retryer.setDefaultTimeout(10);
        }
        private int i = 1;

        public int getInt() throws Exception {
            Thread.sleep(100);
            i++;
            Thread.sleep(100);
            i++;
            return 100;
        }

        public int getInt2() {
            return i;
        }
      }
      """
      Then the following should pass:
      """
      : {
        ::await(0.14s).int::throw: {...}

        int2= 2
      }
      """

    Scenario: default timeout more than waiting time
      Given the following java class:
      """
      public class Data {
        public Data() {
          Retryer.setDefaultTimeout(300);
        }
        public int getInt() throws Exception {
            Thread.sleep(200);
            return 100;
        }
      }
      """
      Then the following should pass:
      """
      : {
        ::await(0.1s).int= 100
      }
      """
