Feature: Summary

  Background:
    Given the following declarations:
      """
      JFactory jFactory = new JFactory();
      """
    And the following bean definition:
      """
      public class Bean {
        public String stringValue;
        public int intValue;
      }
      """

  Rule: Simple Creation

    Scenario: Object Creation - Create an Object with All Default Values
      When evaluating the following code:
        """
        jFactory.create(Bean.class);
        """
      Then the result should be:
        """
        : {
          stringValue= stringValue#1
          intValue= 1
        }
        """

    @import(java.util.*)
    Scenario: Specify Property Value - Create an Object with One or More Specified Property Values
      When evaluating the following code:
        """
        jFactory.type(Bean.class).property("intValue", 100).create()
        """
      Then the result should be:
        """
        : {
          stringValue= stringValue#1
          intValue= 100
        }
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class)
          .property("stringValue", "hello")
          .property("intValue", 43)
          .create();
        """
      Then the result should be:
        """
        : {
          stringValue= hello
          intValue= 43
        }
        """
      When evaluating the following code:
        """
        jFactory.type(Bean.class).properties(new HashMap<String, Object>() {{
          put("stringValue", "world");
          put("intValue", 250);
        }}).create();
        """
      Then the result should be:
        """
        : {
          stringValue= world
          intValue= 250
        }
        """

    Scenario: Auto Type Conversion
      When evaluating the following code:
        """
        jFactory.type(Bean.class)
          .property("stringValue", 100)
          .property("intValue", "200")
          .create();
        """
      Then the result should be:
        """
        : {
          stringValue= '100'
          intValue= 200
        }
        """

  Rule: Use Spec

    Scenario: Spec Class - Define a Spec as a Class and Create an Object by Spec
      Given the following class definition:
        """
        import com.github.leeonky.jfactory.Spec;
        public class ABean extends Spec<Bean> {}
        """
      When evaluating the following code:
        """
        jFactory.spec(ABean.class).property("stringValue", "hello").create();
        """
      Then the result should be:
        """
        stringValue= hello
        """

    Scenario: Spec Name - Use a Spec by Name
      Given the following spec definition:
        """
        public class ABean extends Spec<Bean> {}
        """
      And register as follows:
        """
        jFactory.register(ABean.class);
        """
      When evaluating the following code:
        """
        jFactory.spec("ABean").property("stringValue", "hello").create();
        """
      Then the result should be:
        """
        stringValue= hello
        """

    Scenario: Custom Spec Name - Define a New Spec Name in the Spec Class
      Given the following spec definition:
        """
        public class ABean extends Spec<Bean> {
          protected String getName() { return "OneBean"; }
        }
        """
      And register as follows:
        """
        jFactory.register(ABean.class);
        """
      When evaluating the following code:
        """
        jFactory.spec("OneBean").property("stringValue", "hello").create();
        """
      Then the result should be:
        """
        stringValue= hello
        """

  Rule: Test Data in Spec

    Scenario: Default Value - Define a Default Value for a Property in Spec
      Given the following spec definition:
        """
        public class ABean extends Spec<Bean> {
          public void main() {
            property("stringValue").defaultValue("hello");
          }
        }
        """
      When evaluating the following code:
        """
        jFactory.spec(ABean.class).create();
        """
      Then the result should be:
        """
        stringValue= hello
        """

    Scenario: Spec Value - Define a Spec Value for a Property in Spec
      Given the following spec definition:
        """
        public class ABean extends Spec<Bean> {
          public void main() {
            property("stringValue").value("hello");
          }
        }
        """
      When evaluating the following code:
        """
        jFactory.spec(ABean.class).create();
        """
      Then the result should be:
        """
        stringValue= hello
        """

    Scenario: Value Priority - Spec Value > Default Value
      Given the following spec definition:
        """
        public class ABean extends Spec<Bean> {
          public void main() {
            property("stringValue").value("spec");
            property("stringValue").defaultValue("default");
          }
        }
        """
      When evaluating the following code:
        """
        jFactory.spec(ABean.class).create();
        """
      Then the result should be:
        """
        stringValue= spec
        """

    Scenario: Value Priority - Input Value > Spec Value
      Given the following spec definition:
        """
        public class ABean extends Spec<Bean> {
          public void main() {
            property("stringValue").value("spec");
          }
        }
        """
      When evaluating the following code:
        """
        jFactory.spec(ABean.class).property("stringValue", "input").create();
        """
      Then the result should be:
        """
        stringValue= input
        """

    Scenario: Trait Value - Give value in a Trait and Create with the Trait
      Given the following class definition:
        """
        import com.github.leeonky.jfactory.Spec;
        import com.github.leeonky.jfactory.Trait;
        public class ABean extends Spec<Bean> {
          public void main() {
            property("stringValue").value("hello");
          }

          @Trait
          public void _100() {
            property("intValue").value(100);
          }
        }
        """
      And register as follows:
        """
        jFactory.register(ABean.class);
        """
      When evaluating the following code:
        """
        jFactory.spec("_100", "ABean").create();
        """
      Then the result should be:
        """
        : {
          stringValue= hello
          intValue= 100
        }
        """

  Rule: Data Repository

    Scenario: Save and Query - Create Object and Query It Back
      When execute as follows:
        """
        jFactory.type(Bean.class).property("stringValue", "hello").create();
        jFactory.type(Bean.class).property("stringValue", "world").create();
        """
      And evaluating the following code:
        """
        jFactory.type(Bean.class).queryAll()
        """
      Then the result should be:
        """
        stringValue[]= [hello world]
        """
      And evaluating the following code:
        """
        jFactory.type(Bean.class).property("stringValue", "hello").query()
        """
      Then the result should be:
        """
        : { class.simpleName= Bean, stringValue= hello }
        """

    Scenario: Clear Repo - Clear Repo then Return Empty or Null
      When execute as follows:
        """
        jFactory.type(Bean.class).property("stringValue", "hello").create();
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
      And evaluating the following code:
        """
        jFactory.type(Bean.class).property("stringValue", "hello").query()
        """
      Then the result should be:
        """
        : null
        """

    Scenario: Multiple Result - Should Raise Error when Query with Criteria Return Multiple Objects
      When execute as follows:
        """
        jFactory.type(Bean.class)
          .property("stringValue", "hello")
          .property("intValue", 100).create();
        jFactory.type(Bean.class)
          .property("stringValue", "hello")
          .property("intValue", 200).create();
        """
      And evaluating the following code:
        """
        jFactory.type(Bean.class).property("stringValue", "hello").query()
        """
      Then the result should be:
        """
        ::throw.message= 'There are multiple elements in the query result.'
        """

  Rule: Nested Object Creation

    Background:
      Given the following bean definition:
        """
        public class Author {
          private static int count = 0;
          public Author() { count++; }
          public int created() { return count; }

          public String name, gender;
        }
        """
      And the following bean definition:
        """
        public class Book {
          private static int count = 0;
          public Book() { count++; }
          public int created() { return count; }

          public Author author;
          public String name;
        }
        """
      And the following bean definition:
        """
        public class Bag {
          public Book[] books;
        }
        """

    Scenario: Default Behavior – No Child is Created
      When evaluating the following code:
        """
        jFactory.type(Book.class).create();
        """
      Then the result should be:
        """
        : {
          name= /^name.*/
          author= null
        }
        """

    Scenario: Create New Child with No Spec – Based on Input Child Property Value
      When evaluating the following code:
        """
        jFactory.type(Book.class).property("author.name", "tom").create();
        """
      Then the result should be:
        """
        : {
          name= /^name.*/
          author: {
            name= tom
            created= 1
          }
        }
        """

    Scenario: Associate Existing Child – Associate with Previously Created One When Input Child Property Matches
      Given register as follows:
        """
        jFactory.type(Author.class).property("name", "tom").create();
        """
      And evaluating the following code:
        """
        jFactory.type(Book.class).property("author.name", "tom").create();
        """
      Then the result should be:
        """
        : {
          name= /^name.*/
          author: {
            name= tom
            created= 1
          }
        }
        """

    Scenario: Create New Child from Spec - Based on Input Child Property and Optional Property Spec
      Given the following spec definition:
        """
        public class AnAuthor extends Spec<Author> {
          public void main() {
            property("name").value("tom");
          }
        }
        """
      And the following spec definition:
        """
        public class ABook extends Spec<Book> {
          public void main() {
            property("author").optional("AnAuthor");
          }
        }
        """
      And register as follows:
        """
        jFactory.register(AnAuthor.class);
        """
      When evaluating the following code:
        """
        jFactory.spec(ABook.class).create();
        """
      Then the result should be:
        """
        : {
          name= /^name.*/
          author: null
        }
        """
      When evaluating the following code:
        """
        jFactory.spec(ABook.class).property("author.gender", "male").create();
        """
      Then the result should be:
        """
        : {
          name= /^name.*/
          author: {
            name= tom
            gender= male
          }
        }
        """

    Scenario: Auto Create New Child from Spec - Based on Property Spec Even Without Input Child Property
      Given the following spec definition:
        """
        public class AnAuthor extends Spec<Author> {}
        """
      And the following spec definition:
        """
        public class ABook extends Spec<Book> {
          public void main() {
            property("author").is(AnAuthor.class);
          }
        }
        """
      When evaluating the following code:
        """
        jFactory.spec(ABook.class).create();
        """
      Then the result should be:
        """
        : {
          name= /^name.*/
          author: {
            name= /^name.*/
            gender= /^gender.*/
          }
        }
        """

    Scenario: Default Behavior of Child List - No Element is Created
      When evaluating the following code:
        """
        jFactory.type(Bag.class).create();
        """
      Then the result should be:
        """
        books= null
        """

    Scenario: Create New Element with No Spec – Based on Input Element Property Value
      When evaluating the following code:
        """
        jFactory.type(Bag.class).property("books[0].name", "Spring").create();
        """
      Then the result should be:
        """
        books: [{
          name= Spring
          author= null
        }]
        """

    Scenario: Associate Existing Element – Associate with Previously Created One When Input Element Property Matches
      Given register as follows:
        """
        jFactory.type(Book.class).property("name", "Spring").create();
        """
      When evaluating the following code:
        """
        jFactory.type(Bag.class).property("books[0].name", "Spring").create();
        """
      Then the result should be:
        """
        books: [{
          name= Spring
          author= null
          created= 1
        }]
        """

    Scenario: Create New Element from Spec - Based on Input Element Property and Element Spec
      Given the following spec definition:
        """
        public class AnAuthor extends Spec<Author> {}
        """
      Given the following spec definition:
        """
        public class ABook extends Spec<Book> {
          public void main() {
            property("author").is(AnAuthor.class);
          }
        }
        """
      Given the following spec definition:
        """
        public class ABag extends Spec<Bag> {
          public void main() {
            property("books[]").is(ABook.class);
          }
        }
        """
      When evaluating the following code:
        """
        jFactory.spec(ABag.class).create();
        """
      Then the result should be:
        """
        books: []
        """
      When evaluating the following code:
        """
        jFactory.spec(ABag.class).property("books[0].name", "Spring").create();
        """
      Then the result should be:
        """
        books: [{
          name= Spring
          author= {
            name= /^name.*/
            gender= /^gender.*/
          }
        }]
        """

  Rule: Custom Repository / Data Source

    @import(java.util.*)
    Scenario: Custom Save - Implement Custom Save to Persistent Object to Other Data Source
      Given the following declarations:
        """
        List<Object> createdList = new ArrayList<>();
        """
      And the following declarations:
        """
        JFactory jFactoryWithRepo = new JFactory(new DataRepository() {
          @Override
          public void save(Object object) { createdList.add(object); }
        });
        """
      When execute as follows:
        """
        jFactoryWithRepo.type(Bean.class).property("stringValue", "hello").create();
        """
      Then the field "createdList" should be:
        """
        : [{ class.simpleName= Bean, stringValue= hello }]
        """
      And execute as follows:
        """
        jFactoryWithRepo.type(Bean.class).property("stringValue", "world").create();
        """
      Then the field "createdList" should be:
        """
        : | class.simpleName | stringValue |
          | Bean             | hello       |
          | Bean             | world       |
        """

    @import(java.util.*)
    Scenario: Custom Query - Implement Custom Query to Query Object from Custom Data Source
      Given the following declarations:
        """
        List<Object> createdList = new ArrayList<>();
        """
      And the following declarations:
        """
        JFactory jFactoryWithRepo = new JFactory(new DataRepository() {
          @Override
          public <T> Collection<T> queryAll(Class<T> type) {
            return (Collection<T>)createdList;
          }
        });
        """
      When execute as follows:
        """
        Bean bean = new Bean();
        bean.stringValue = "hello";
        createdList.add(bean);
        """
      And evaluating the following code:
        """
        jFactoryWithRepo.type(Bean.class).queryAll();
        """
      Then the result should be:
        """
        : [{ class.simpleName= Bean, stringValue= hello }]
        """

    @import(java.util.*)
    Scenario: Multiple Data Source - Composite Repository for Multiple Data Source
      Given the following bean definition:
        """
        public class DBEntity {
          public String value;
        }
        """
      Given the following bean definition:
        """
        public class RedisData {
          public String value;
        }
        """
      Given the following declarations:
        """
        List<Object> createdDBs = new ArrayList<>();
        List<Object> createdRedis = new ArrayList<>();
        """
      And the following declarations:
        """
        DataRepository dbRepo = new DataRepository() {
          @Override
          public void save(Object object) { createdDBs.add(object); }
        };

        DataRepository redisRepo = new DataRepository() {
          @Override
          public void save(Object object) { createdRedis.add(object); }
        };

        CompositeDataRepository compositeRepo = new CompositeDataRepository() {{
          registerByType(DBEntity.class, dbRepo);
          registerByType(RedisData.class, redisRepo);
        }};

        JFactory jFactoryWithRepo = new JFactory(compositeRepo);
        """
      When execute as follows:
        """
        jFactoryWithRepo.type(DBEntity.class).property("value", "db").create();
        jFactoryWithRepo.type(RedisData.class).property("value", "redis").create();
        """
      Then the field "createdDBs" should be:
        """
        : [{class.simpleName= DBEntity, value= db}]
        """
      Then the field "createdRedis" should be:
        """
        : [{class.simpleName= RedisData, value= redis}]
        """

  Rule: Save Order / Reverse Association

    Background:
      Given the following declarations:
        """
        List<Object> createdList = new ArrayList<>();
        """
      Given the following declarations:
        """
        JFactory jFactoryWithRepo = new JFactory(new DataRepository() {
          @Override
          public void save(Object object) { createdList.add(object); }
        });
        """

    @import(java.util.*)
    Scenario: Default Behavior - Save Child Before Parent
      Given the following bean definition:
        """
        public class Car {
          public String model;
          public Engine engine;
        }
        """
      Given the following bean definition:
        """
        public class Engine {
          public Car car;
          public String number;
        }
        """
      When evaluating the following code:
        """
        jFactoryWithRepo.type(Car.class).property("model", "X").property("engine.number", "e01").create();
        """
      Then the result should be:
        """
        : {
          model: X
          engine: {
            number: e01
            car= null
          }
        }
        """
      And the field "createdList" should be:
        """
        class[].simpleName: [Engine Car]
        """

    @import(java.util.*)
    Scenario: Reverse Association - Save Parent First, Assign Parent to Child’s Back-Reference, Then Save Child
      Given the following bean definition:
        """
        public class Car {
          public String model;
          public Engine engine;
        }
        """
      Given the following bean definition:
        """
        public class Engine {
          public Car car;
          public String number;
        }
        """
      Given the following spec definition:
        """
        public class ACar extends Spec<Car> {
          public void main() {
            property("engine").reverseAssociation("car");
          }
        }
        """
      Given the following spec definition:
        """
        public class AnEngine extends Spec<Engine> {}
        """
      When evaluating the following code:
        """
        jFactoryWithRepo.spec(ACar.class).property("model", "X").property("engine.number", "e01").create();
        """
      Then the result should be:
        """
        : {
          model: X
          engine: {
            number: e01
            car= ::root
          }
        }
        """
      And the field "createdList" should be:
        """
        class[].simpleName: [Car Engine]
        """

    @import(java.util.*)
    Scenario: Reverse Association for List - Save Parent First, Assign Parent to Each Child’s Back-Reference, Then Save All Children
      Given the following bean definition:
        """
        public class Order {
          public String id;
          public OrderLine lines[];
        }
        """
      Given the following bean definition:
        """
        public class OrderLine {
          public Order order;
          public String product;
        }
        """
      Given the following spec definition:
        """
        public class AnOrder extends Spec<Order> {
          public void main() {
            property("lines").reverseAssociation("order");
          }
        }
        """
      When evaluating the following code:
        """
        jFactoryWithRepo.spec(AnOrder.class)
          .property("id", "s01")
          .property("lines[0].product", "PC")
          .property("lines[1].product", "IPAD")
          .create();
        """
      Then the result should be:
        """
        : {
          id= s01
          lines: | product | order  |
                 | PC      | ::root |
                 | IPAD    | ::root |
        }
        """
      And the field "createdList" should be:
        """
        class[].simpleName: [Order OrderLine OrderLine]
        """
