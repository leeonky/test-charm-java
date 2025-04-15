Feature: single or list

  Rule: as list

    Scenario: consider data as list
      Given the following java class:
        """
        public class Test {
          public AdaptiveList<Integer> getList() {
            return new AdaptiveList<Integer>(Arrays.asList(1, 2, 3));
          }
        }
        """
      Then the following verification for the instance of java class "Test" should pass:
        """
        list: [1 2 3]
        """
      When use a instance of java class "Test" to evaluate:
        """
        list: [1 2]
        """
      Then failed with the message:
        """
        Unexpected list size
        Expected: <2>
        Actual: <3>
        Actual list: com.github.leeonky.dal.runtime.AdaptiveList [
            java.lang.Integer <1>,
            java.lang.Integer <2>,
            java.lang.Integer <3>
        ]
        """

    Scenario: meta size should return list size
      Given the following java class:
        """
        public class Test {
          public AdaptiveList<Integer> getList() {
            return new AdaptiveList<Integer>(Arrays.asList(1, 2, 3));
          }
        }
        """
      Then the following verification for the instance of java class "Test" should pass:
        """
        list::size= 3
        """
      When use a instance of java class "Test" to evaluate:
        """
        list::size= 2
        """
      Then failed with the message:
        """
        Expected to be equal to: java.lang.Integer
        <2>
         ^
        Actual: java.lang.Integer
        <3>
         ^
        """

  Rule: solo element

    Scenario: When accessing the list property, the access is delegated to the sole element of the list
      Given the following java class:
        """
        public class Test {
          public AdaptiveList<String> getList() {
            return new AdaptiveList<String>(Arrays.asList("hello"));
          }
        }
        """
      Then the following verification for the instance of java class "Test" should pass:
        """
        list.toUpperCase: "HELLO"
        """
      And the following verification for the instance of java class "Test" should pass:
        """
        list: {
          toUpperCase: "HELLO"
        }
        """

    Scenario: access property should raise error when list size is not 1
      Given the following java class:
        """
        public class Test {
          public AdaptiveList<String> getList() {
            return new AdaptiveList<String>(Arrays.asList("hello", "world"));
          }
        }
        """
      When use a instance of java class "Test" to evaluate:
        """
        list.toUpperCase: "HELLO"
        """
      Then failed with the message:
        """
        Get property `toUpperCase` failed, property can be:
          1. public field
          2. public getter
          3. public method
          4. Map key value
          5. customized type getter
          6. static method extension
        java.lang.IllegalStateException: Expected only one element
        """
      And got the following notation:
        """
        list.toUpperCase: "HELLO"
             ^
        """
      When use a instance of java class "Test" to evaluate:
        """
        list: {
          toUpperCase: "HELLO"
        }
        """
      Then failed with the message:
        """
        Get property `toUpperCase` failed, property can be:
          1. public field
          2. public getter
          3. public method
          4. Map key value
          5. customized type getter
          6. static method extension
        java.lang.IllegalStateException: Expected only one element
        """
      And got the following notation:
        """
        list: {
          toUpperCase: "HELLO"
          ^
        }
        """

    Scenario: When accessing the list filed names, the access is delegated to the sole element of the list
      Given the following java class:
        """
        public class Data {
          public int i=1, j=2;
        }
        """
      Given the following java class:
        """
        public class Test {
          public AdaptiveList<Data> getList() {
            return new AdaptiveList<Data>(Arrays.asList(new Data()));
          }
        }
        """
      And the following verification for the instance of java class "Test" should pass:
        """
        list= {
          i= 1
          j= 2
        }
        """
      When use a instance of java class "Test" to evaluate:
        """
        list= {
          i= 1
        }
        """
      Then failed with the message:
        """
        Unexpected fields `j` in list
        """
      And got the following notation:
        """
        list= {
            ^
          i= 1
        }
        """

    Scenario: When accessing the list meta property, the access is delegated to the sole element of the list
      Given the following java class:
        """
        public class Data {
          public int i=1, j=2;
        }
        """
      Given the following java class:
        """
        public class Test {
          public AdaptiveList<Data> getList() {
            return new AdaptiveList<Data>(Arrays.asList(new Data()));
          }
        }
        """
      And register DAL:
        """
        dal.getRuntimeContextBuilder()
          .registerMetaProperty(Data.class, "meta", m->m.data().value().i);
        """
      Then the following verification for the instance of java class "Test" should pass:
        """
        list::meta= 1
        """
      And the following verification for the instance of java class "Test" should pass:
        """
        list: {
          ::meta= 1
        }
        """
      When use a instance of java class "Test" to evaluate:
        """
        list::meta= 2
        """
      Then failed with the message:
        """
        Expected to be equal to: java.lang.Integer
        <2>
         ^
        Actual: java.lang.Integer
        <1>
         ^
        """
      And got the following notation:
        """
        list::meta= 2
                    ^
        """
      When use a instance of java class "Test" to evaluate:
        """
        list: {
          ::meta= 2
        }
        """
      Then failed with the message:
        """
        Expected to be equal to: java.lang.Integer
        <2>
         ^
        Actual: java.lang.Integer
        <1>
         ^
        """
      And got the following notation:
        """
        list: {
          ::meta= 2
                  ^
        }
        """

    Scenario: access meta property should raise error when list size is not 1
      Given the following java class:
        """
        public class Data {
          public int i=1, j=2;
        }
        """
      Given the following java class:
        """
        public class Test {
          public AdaptiveList<Data> getList() {
              return new AdaptiveList<Data>(Arrays.asList(new Data(), new Data()));
          }
        }
        """
      When use a instance of java class "Test" to evaluate:
        """
        list::meta= 1
        """
      Then failed with the message:
        """
        java.lang.IllegalStateException: Expected only one element
        """
      And got the following notation:
        """
        list::meta= 1
              ^
        """

#  first index
#  unlimited list
