Feature: System Default Value

  Please don't rely on the system default value in your test as it may change in the future.

  Background:
    Given the following bean class:
      """
      public class CustomObject {
        public String anyProperty;
      }
      """
    And declaration jFactory =
      """
      new JFactory(new DataRepository() {
        @Override
        public void save(Object object) {
        }
        @Override
        public <T> Collection<T> queryAll(Class<T> type) {
            return new ArrayList<T>();
        }
        @Override
        public void clear() {
        }
      });
      """

  Scenario: first and second system default value for bean property
    Given the following bean class:
      """
      public class Bean {
        public String stringValue;
        public int intValue;
        public Integer boxedIntValue;
        public short shortValue;
        public Short boxedShortValue;
        public byte byteValue;
        public Byte boxedbyteValue;
        public long longValue;
        public Long boxedLongvalue;
        public float floatValue;
        public Float boxedFloatValue;
        public double doubleValue;
        public Double boxedDoubleValue;
        public boolean boolValue;
        public Boolean boxedBoolValue;
        public BigInteger bigInt;
        public BigDecimal bigDec;
        public UUID uuid;
        public Date date;
        public java.time.Instant instant;
        public java.time.LocalDate localDate;
        public java.time.LocalTime localTime;
        public java.time.LocalDateTime localDateTime;
        public java.time.OffsetDateTime offsetDateTime;
        public java.time.ZonedDateTime zonedDateTime;
        public EnumType enumValue;

        public enum EnumType {
          A, B
        }
      }
      """
    When build:
      """
      jFactory.type(Bean.class).create();
      """
    Then the result should:
      """
      = {
        stringValue= stringValue#1
        intValue= 1
        boxedIntValue= 1
        shortValue= 1s
        boxedShortValue= 1s
        byteValue= 1y
        boxedbyteValue= 1y
        longValue= 1L
        boxedLongvalue= 1L
        floatValue= 1.0f
        boxedFloatValue= 1.0f
        doubleValue= 1.0d
        boxedDoubleValue= 1.0d
        boolValue= true
        boxedBoolValue= true
        bigInt= 1bi
        bigDec= 1bd
        uuid: '00000000-0000-0000-0000-000000000001'
        date.toInstant: '1996-01-24T00:00:00Z'
        instant: '1996-01-23T00:00:01Z'
        localDate: '1996-01-24'
        localTime: '00:00:01'
        localDateTime: '1996-01-23T00:00:01'
        offsetDateTime.toInstant: '1996-01-23T00:00:01Z'
        zonedDateTime.toInstant: '1996-01-23T00:00:01Z'
        enumValue: A
      }
      """
    When build:
      """
      jFactory.type(Bean.class).create();
      """
    Then the result should:
      """
      = {
        stringValue= stringValue#2
        intValue= 2
        boxedIntValue= 2
        shortValue= 2s
        boxedShortValue= 2s
        byteValue= 2y
        boxedbyteValue= 2y
        longValue= 2L
        boxedLongvalue= 2L
        floatValue= 2.0f
        boxedFloatValue= 2.0f
        doubleValue= 2.0d
        boxedDoubleValue= 2.0d
        boolValue= false
        boxedBoolValue= false
        bigInt= 2bi
        bigDec= 2bd
        uuid: '00000000-0000-0000-0000-000000000002'
        date.toInstant: '1996-01-25T00:00:00Z'
        instant: '1996-01-23T00:00:02Z'
        localDate: '1996-01-25'
        localTime: '00:00:02'
        localDateTime: '1996-01-23T00:00:02'
        offsetDateTime.toInstant: '1996-01-23T00:00:02Z'
        zonedDateTime.toInstant: '1996-01-23T00:00:02Z'
        enumValue: B
      }
      """

  Scenario: first and second system default value for bean property with custom type
    Given the following bean class:
      """
      public class Bean {
        public java.time.YearMonth yearMonth;
        public CustomObject customObject;
      }
      """
    When build:
      """
      jFactory.type(Bean.class).create();
      """
    Then the result should:
      """
      = {
        yearMonth= null
        customObject= null
      }
      """
    When build:
      """
      jFactory.type(Bean.class).create();
      """
    Then the result should:
      """
      = {

        yearMonth= null
        customObject= null
      }
      """

  Rule: recursive system default value for bean property

    Scenario Outline: byte/Byte/short/Short/boolean/Boolean, for int/Integer/long/Long single test runs too long time so have to be ignored
      Given the following bean class:
      """
      public class Bean {
        public <primitiveType> primitiveValue;
        public <classType> classTypeValue;
      }
      """
      When register:
      """
      IntStream.range(0, <sequence>).forEach(i -> {
        jFactory.type(Bean.class).create();
      });
      """
      When build:
      """
      jFactory.type(Bean.class).create();
      """
      Then the result should:
      """
      : {
        primitiveValue: <value>
        classTypeValue: <value>
      }
      """
      Examples:
        | primitiveType | classType | sequence            | value   |
        | byte          | Byte      | Byte.MAX_VALUE-1    | 127y    |
        | byte          | Byte      | Byte.MAX_VALUE      | -128    |
        | byte          | Byte      | Byte.MAX_VALUE+1    | -127y   |
        | byte          | Byte      | Byte.MAX_VALUE*2    | -1y     |
        | byte          | Byte      | Byte.MAX_VALUE*2+1  | 0y      |
        | byte          | Byte      | Byte.MAX_VALUE*2+2  | 1y      |
        | short         | Short     | Short.MAX_VALUE-1   | 32767s  |
        | short         | Short     | Short.MAX_VALUE     | -32768  |
        | short         | Short     | Short.MAX_VALUE+1   | -32767s |
        | short         | Short     | Short.MAX_VALUE*2   | -1s     |
        | short         | Short     | Short.MAX_VALUE*2+1 | 0s      |
        | short         | Short     | Short.MAX_VALUE*2+2 | 1s      |
        | boolean       | Boolean   | 2                   | true    |

    Scenario Outline: LocalTime
      Given the following bean class:
      """
      public class Bean {
        public <type> value;
      }
      """
      When register:
      """
      IntStream.range(0, <sequence>).forEach(i -> {
        jFactory.type(Bean.class).create();
      });
      """
      When build:
      """
      jFactory.type(Bean.class).create();
      """
      Then the result should:
      """
      value: <value>
      """
      Examples:
        | type                | sequence | value      |
        | java.time.LocalTime | 24*60*60 | '00:00:01' |

    Scenario: Enum
      Given the following bean class:
      """
      public class Bean {
          public EnumType enumValue;
          public enum EnumType {
            A, B
          }
      }
      """
      When register:
      """
      IntStream.range(0, 2).forEach(i -> {
        jFactory.type(Bean.class).create();
      });
      """
      When build:
      """
      jFactory.type(Bean.class).create();
      """
      Then the result should:
      """
      enumValue: A
      """

  Rule: bean list property default value

    Scenario Outline: String/int/Integer/short/Short/long/Long/byte/Byte/float/Float/double/Double/BigInteger/BigDecimal/boolean/Boolean/UUID/Date/Instant/LocalDate/LocalTime/LocalDateTime/OffsetDateTime/ZonedDateTime/Enum list with no element value specified
      Given the following bean class:
      """
      public class Bean {
        public <type> values;
        public enum EnumType {
          A, B
        }
      }
      """
      When build:
      """
      jFactory.type(Bean.class).create();
      """
      Then the result should:
      """
      values: null
      """
      Examples:
        | type                           |
        | String[]                       |
        | List<String>                   |
        | Set<String>                    |
        | int[]                          |
        | Integer[]                      |
        | List<Integer>                  |
        | Set<Integer>                   |
        | short[]                        |
        | Short[]                        |
        | List<Short>                    |
        | Set<Short>                     |
        | long[]                         |
        | Long[]                         |
        | List<Long>                     |
        | Set<Long>                      |
        | byte[]                         |
        | Byte[]                         |
        | List<Byte>                     |
        | Set<Byte>                      |
        | float[]                        |
        | Float[]                        |
        | List<Float>                    |
        | Set<Float>                     |
        | double[]                       |
        | Double[]                       |
        | List<Double>                   |
        | Set<Double>                    |
        | BigInteger[]                   |
        | List<BigInteger>               |
        | Set<BigInteger>                |
        | BigDecimal[]                   |
        | List<BigDecimal>               |
        | Set<BigDecimal>                |
        | boolean[]                      |
        | Boolean[]                      |
        | List<Boolean>                  |
        | Set<Boolean>                   |
        | UUID[]                         |
        | List<UUID>                     |
        | Set<UUID>                      |
        | java.time.Instant[]            |
        | List<java.time.Instant>        |
        | Set<java.time.Instant>         |
        | java.time.LocalDate[]          |
        | List<java.time.LocalDate>      |
        | Set<java.time.LocalDate>       |
        | java.time.LocalTime[]          |
        | List<java.time.LocalTime>      |
        | Set<java.time.LocalTime>       |
        | java.time.LocalDateTime[]      |
        | List<java.time.LocalDateTime>  |
        | Set<java.time.LocalDateTime>   |
        | java.time.OffsetDateTime[]     |
        | List<java.time.OffsetDateTime> |
        | Set<java.time.OffsetDateTime>  |
        | java.time.ZonedDateTime[]      |
        | List<java.time.ZonedDateTime>  |
        | Set<java.time.ZonedDateTime>   |
        | Date[]                         |
        | List<Date>                     |
        | Set<Date>                      |
        | EnumType[]                     |
        | List<EnumType>                 |
        | Set<EnumType>                  |

    Scenario Outline: Custom type list with no element value specified
      Given the following bean class:
      """
      public class Bean {
        public <type> values;
        public enum EnumType {
          A, B
        }
      }
      """
      When build:
      """
      jFactory.type(Bean.class).create();
      """
      Then the result should:
      """
      values: null
      """
      Examples:
        | type                      |
        | java.time.YearMonth[]     |
        | List<java.time.YearMonth> |
        | Set<java.time.YearMonth>  |
        | CustomObject[]            |
        | List<CustomObject>        |
        | Set<CustomObject>         |

    Scenario Outline: String/int/Integer/short/Short/long/Long/byte/Byte/float/Float/double/Double/BigInteger/BigDecimal/boolean/Boolean/UUID/Instant/LocalDate/LocalTime/LocalDateTime list with one element value specified
      Given the following bean class:
      """
      public class Bean {
        public <type> values;
      }
      """
      When build:
      """
      jFactory.type(Bean.class).property("values[1]", <specifiedValue>).create();
      """
      Then the result should:
      """
      values: [<defaultValue>, <specifiedValue>]
      """
      Examples:
        | type                          | specifiedValue                         | defaultValue                           |
        | String[]                      | "hello"                                | "values#1[0]"                          |
        | List<String>                  | "hello"                                | "values#1[0]"                          |
        | Set<String>                   | "hello"                                | "values#1[0]"                          |
        | int[]                         | 42                                     | 1                                      |
        | Integer[]                     | 42                                     | 1                                      |
        | List<Integer>                 | 42                                     | 1                                      |
        | Set<Integer>                  | 42                                     | 1                                      |
        | short[]                       | 42                                     | 1                                      |
        | Short[]                       | 42                                     | 1                                      |
        | List<Short>                   | 42                                     | 1                                      |
        | Set<Short>                    | 42                                     | 1                                      |
        | long[]                        | 42                                     | 1                                      |
        | Long[]                        | 42                                     | 1                                      |
        | List<Long>                    | 42                                     | 1                                      |
        | Set<Long>                     | 42                                     | 1                                      |
        | byte[]                        | 42                                     | 1                                      |
        | Byte[]                        | 42                                     | 1                                      |
        | List<Byte>                    | 42                                     | 1                                      |
        | Set<Byte>                     | 42                                     | 1                                      |
        | float[]                       | 42.0f                                  | 1.0f                                   |
        | Float[]                       | 42.0f                                  | 1.0f                                   |
        | List<Float>                   | 42.0f                                  | 1.0f                                   |
        | Set<Float>                    | 42.0f                                  | 1.0f                                   |
        | double[]                      | 42.0d                                  | 1.0d                                   |
        | Double[]                      | 42.0d                                  | 1.0d                                   |
        | List<Double>                  | 42.0d                                  | 1.0d                                   |
        | Set<Double>                   | 42.0d                                  | 1.0d                                   |
        | BigInteger[]                  | 42                                     | 1                                      |
        | List<BigInteger>              | 42                                     | 1                                      |
        | Set<BigInteger>               | 42                                     | 1                                      |
        | BigDecimal[]                  | 42.0                                   | 1.0                                    |
        | List<BigDecimal>              | 42.0                                   | 1.0                                    |
        | Set<BigDecimal>               | 42.0                                   | 1.0                                    |
        | boolean[]                     | true                                   | true                                   |
        | Boolean[]                     | true                                   | true                                   |
        | List<Boolean>                 | true                                   | true                                   |
        | UUID[]                        | "5abbd538-3ef6-4391-8e4f-6dc9f98ac505" | "00000000-0000-0000-0000-000000000001" |
        | List<UUID>                    | "5abbd538-3ef6-4391-8e4f-6dc9f98ac505" | "00000000-0000-0000-0000-000000000001" |
        | Set<UUID>                     | "5abbd538-3ef6-4391-8e4f-6dc9f98ac505" | "00000000-0000-0000-0000-000000000001" |
        | java.time.Instant[]           | "2023-06-25T08:48:03Z"                 | "1996-01-23T00:00:01Z"                 |
        | List<java.time.Instant>       | "2023-06-25T08:48:03Z"                 | "1996-01-23T00:00:01Z"                 |
        | Set<java.time.Instant>        | "2023-06-25T08:48:03Z"                 | "1996-01-23T00:00:01Z"                 |
        | java.time.LocalDate[]         | "2023-06-25"                           | "1996-01-24"                           |
        | List<java.time.LocalDate>     | "2023-06-25"                           | "1996-01-24"                           |
        | Set<java.time.LocalDate>      | "2023-06-25"                           | "1996-01-24"                           |
        | java.time.LocalTime[]         | "08:48:03"                             | "00:00:01"                             |
        | List<java.time.LocalTime>     | "08:48:03"                             | "00:00:01"                             |
        | Set<java.time.LocalTime>      | "08:48:03"                             | "00:00:01"                             |
        | java.time.LocalDateTime[]     | "2023-06-25T08:48:03"                  | "1996-01-23T00:00:01"                  |
        | List<java.time.LocalDateTime> | "2023-06-25T08:48:03"                  | "1996-01-23T00:00:01"                  |
        | Set<java.time.LocalDateTime>  | "2023-06-25T08:48:03"                  | "1996-01-23T00:00:01"                  |

    Scenario: Boolean Set with one element value specified
      Given the following bean class:
      """
      public class Bean {
        public Set<Boolean> values;
      }
      """
      When build:
      """
      jFactory.type(Bean.class).property("values[1]", true).create();
      """
      Then the result should:
      """
      values: [true]
      """

    Scenario Outline: Date list with one element value specified
      Given the following bean class:
      """
      public class Bean {
          public <type> values;
      }
      """
      When build:
      """
      jFactory.type(Bean.class).property("values[1]", "2023-06-25").create();
      """
      Then the result should:
      """
      : {
        values[0].toInstant: "1996-01-24T00:00:00Z"
        values[1].toInstant: "2023-06-25T00:00:00Z"
      }
      """
      Examples:
        | type       |
        | Date[]     |
        | List<Date> |
        | Set<Date>  |

    Scenario Outline: OffsetDateTime/ZonedDateTime list with one element value specified
      Given the following bean class:
      """
      public class Bean {
          public <type> values;
      }
      """
      When build:
      """
      jFactory.type(Bean.class).property("values[1]", "2023-06-25T06:48:03Z").create();
      """
      Then the result should:
      """
      : {
        values[0].toInstant: "1996-01-23T00:00:01Z"
        values[1].toInstant: "2023-06-25T06:48:03Z"
      }
      """
      Examples:
        | type                           |
        | java.time.OffsetDateTime[]     |
        | List<java.time.OffsetDateTime> |
        | Set<java.time.OffsetDateTime>  |
        | java.time.ZonedDateTime[]      |
        | List<java.time.ZonedDateTime>  |
        | Set<java.time.ZonedDateTime>   |

    Scenario Outline: Enum list with one element value specified
      Given the following bean class:
      """
      public class Bean {
          public <type> values;
          public enum EnumType {
            A, B
          }
      }
      """
      When build:
      """
      jFactory.type(Bean.class).property("values[1]", "B").create();
      """
      Then the result should:
      """
      values: [A, B]
      """
      Examples:
        | type           |
        | EnumType[]     |
        | List<EnumType> |
        | Set<EnumType>  |

    Scenario Outline: Custom type list with one element value specified
      Given the following bean class:
      """
      public class Bean {
        public <type> values;
      }
      """
      When build:
      """
      jFactory.type(Bean.class).property("values[1]", <specifiedValue>).create();
      """
      Then the result should:
      """
      values: [<defaultValue>, <expectedSpecifiedValue>]
      """
      Examples:
        | type                      | specifiedValue     | defaultValue | expectedSpecifiedValue |
        | java.time.YearMonth[]     | "2023-06"          | null         | '2023-06'              |
        | List<java.time.YearMonth> | "2023-06"          | null         | '2023-06'              |
        | Set<java.time.YearMonth>  | "2023-06"          | null         | '2023-06'              |
        | CustomObject[]            | new CustomObject() | null         | {...}                  |
        | List<CustomObject>        | new CustomObject() | null         | {...}                  |
        | Set<CustomObject>         | new CustomObject() | null         | {...}                  |

    Scenario Outline: String/int/Integer/short/Short/long/Long/byte/Byte/float/Float/double/Double/BigInteger/BigDecimal/boolean/Boolean/UUID/Instant/LocalDate/LocalTime/LocalDateTime list with more than one elements value specified
      Given the following bean class:
      """
      public class Bean {
          public <type> values;
      }
      """
      When build:
      """
      jFactory.type(Bean.class).property("values[1]", <firstSpecifiedValue>).property("values[3]", <secondSpecifiedValue>).create();
      """
      Then the result should:
      """
      values: [<firstDefaultValue>, <firstSpecifiedValue>, <secondDefaultValue>, <secondSpecifiedValue>]
      """
      Examples:
        | type                          | firstSpecifiedValue                    | secondSpecifiedValue                   | firstDefaultValue                      | secondDefaultValue                     |
        | String[]                      | "hello"                                | "world"                                | "values#1[0]"                          | "values#1[2]"                          |
        | List<String>                  | "hello"                                | "world"                                | "values#1[0]"                          | "values#1[2]"                          |
        | Set<String>                   | "hello"                                | "world"                                | "values#1[0]"                          | "values#1[2]"                          |
        | int[]                         | 42                                     | 12306                                  | 1                                      | 1                                      |
        | Integer[]                     | 42                                     | 12306                                  | 1                                      | 1                                      |
        | List<Integer>                 | 42                                     | 12306                                  | 1                                      | 1                                      |
        | short[]                       | 42                                     | 12306                                  | 1                                      | 1                                      |
        | Short[]                       | 42                                     | 12306                                  | 1                                      | 1                                      |
        | List<Short>                   | 42                                     | 12306                                  | 1                                      | 1                                      |
        | long[]                        | 42                                     | 12306                                  | 1                                      | 1                                      |
        | Long[]                        | 42                                     | 12306                                  | 1                                      | 1                                      |
        | List<Long>                    | 42                                     | 12306                                  | 1                                      | 1                                      |
        | byte[]                        | 42                                     | 75                                     | 1                                      | 1                                      |
        | Byte[]                        | 42                                     | 75                                     | 1                                      | 1                                      |
        | List<Byte>                    | 42                                     | 75                                     | 1                                      | 1                                      |
        | float[]                       | 42.0                                   | 75.0                                   | 1.0                                    | 1.0                                    |
        | Float[]                       | 42.0                                   | 75.0                                   | 1.0                                    | 1.0                                    |
        | List<Float>                   | 42.0                                   | 75.0                                   | 1.0                                    | 1.0                                    |
        | double[]                      | 42.0                                   | 75.0                                   | 1.0                                    | 1.0                                    |
        | Double[]                      | 42.0                                   | 75.0                                   | 1.0                                    | 1.0                                    |
        | List<Double>                  | 42.0                                   | 75.0                                   | 1.0                                    | 1.0                                    |
        | BigInteger[]                  | 42                                     | 75                                     | 1                                      | 1                                      |
        | List<BigInteger>              | 42                                     | 75                                     | 1                                      | 1                                      |
        | BigDecimal[]                  | 42.0                                   | 75.0                                   | 1.0                                    | 1.0                                    |
        | List<BigDecimal>              | 42.0                                   | 75.0                                   | 1.0                                    | 1.0                                    |
        | boolean[]                     | true                                   | false                                  | true                                   | true                                   |
        | Boolean[]                     | true                                   | false                                  | true                                   | true                                   |
        | List<Boolean>                 | true                                   | false                                  | true                                   | true                                   |
        | UUID[]                        | "5b5e9230-3b4e-4b6e-8f0d-0b9e8e0e6c6d" | "7f6e5d4c-3b2a-1b0c-9a8b-7c6d5e4f3a2b" | "00000000-0000-0000-0000-000000000001" | "00000000-0000-0000-0000-000000000001" |
        | List<UUID>                    | "5b5e9230-3b4e-4b6e-8f0d-0b9e8e0e6c6d" | "7f6e5d4c-3b2a-1b0c-9a8b-7c6d5e4f3a2b" | "00000000-0000-0000-0000-000000000001" | "00000000-0000-0000-0000-000000000001" |
        | java.time.Instant[]           | "2023-06-25T08:48:03Z"                 | "2023-07-08T08:48:03Z"                 | "1996-01-23T00:00:01Z"                 | "1996-01-23T00:00:01Z"                 |
        | List<java.time.Instant>       | "2023-06-25T08:48:03Z"                 | "2023-07-08T08:48:03Z"                 | "1996-01-23T00:00:01Z"                 | "1996-01-23T00:00:01Z"                 |
        | java.time.LocalDate[]         | "2023-06-25"                           | "2023-07-08"                           | "1996-01-24"                           | "1996-01-24"                           |
        | List<java.time.LocalDate>     | "2023-06-25"                           | "2023-07-08"                           | "1996-01-24"                           | "1996-01-24"                           |
        | java.time.LocalTime[]         | "08:48:03"                             | "17:26:03"                             | "00:00:01"                             | "00:00:01"                             |
        | List<java.time.LocalTime>     | "08:48:03"                             | "17:26:03"                             | "00:00:01"                             | "00:00:01"                             |
        | java.time.LocalDateTime[]     | "2023-06-25T08:48:03"                  | "2023-07-08T17:26:03"                  | "1996-01-23T00:00:01"                  | "1996-01-23T00:00:01"                  |
        | List<java.time.LocalDateTime> | "2023-06-25T08:48:03"                  | "2023-07-08T17:26:03"                  | "1996-01-23T00:00:01"                  | "1996-01-23T00:00:01"                  |
        | boolean[]                     | true                                   | false                                  | true                                   | true                                   |

    Scenario Outline: Integer/Short/Long/Byte/Float/Double/BigInteger/BigDecimal/UUID/Instant/LocalDate/LocalDateTime Set with more than one elements value specified
      Given the following bean class:
      """
      public class Bean {
          public <type> values;
      }
      """
      When build:
      """
      jFactory.type(Bean.class).property("values[1]", <firstSpecifiedValue>).property("values[3]", <secondSpecifiedValue>).create();
      """
      Then the result should:
      """
      values: [<firstDefaultValue>, <firstSpecifiedValue>, <secondSpecifiedValue>]
      """
      Examples:
        | type                         | firstSpecifiedValue                    | secondSpecifiedValue                   | firstDefaultValue                      |
        | Set<Integer>                 | 42                                     | 75                                     | 1                                      |
        | Set<Short>                   | 42                                     | 75                                     | 1                                      |
        | Set<Long>                    | 42                                     | 75                                     | 1                                      |
        | Set<Byte>                    | 42                                     | 75                                     | 1                                      |
        | Set<Float>                   | 42.0                                   | 75.0                                   | 1.0                                    |
        | Set<Double>                  | 42.0                                   | 75.0                                   | 1.0                                    |
        | Set<BigInteger>              | 42                                     | 75                                     | 1                                      |
        | Set<BigDecimal>              | 42.0                                   | 75.0                                   | 1.0                                    |
        | Set<UUID>                    | "5b5e9230-3b4e-4b6e-8f0d-0b9e8e0e6c6d" | "7f6e5d4c-3b2a-1b0c-9a8b-7c6d5e4f3a2b" | "00000000-0000-0000-0000-000000000001" |
        | Set<java.time.Instant>       | "2023-06-25T08:48:03Z"                 | "2023-07-08T08:48:03Z"                 | "1996-01-23T00:00:01Z"                 |
        | Set<java.time.LocalDate>     | "2023-06-25"                           | "2023-07-08"                           | "1996-01-24"                           |
        | Set<java.time.LocalTime>     | "08:48:03"                             | "17:26:03"                             | "00:00:01"                             |
        | Set<java.time.LocalDateTime> | "2023-06-25T08:48:03"                  | "2023-07-08T17:26:03"                  | "1996-01-23T00:00:01"                  |

    Scenario: Boolean Set with more than one elements value specified
      Given the following bean class:
      """
      public class Bean {
          public Set<Boolean> values;
      }
      """
      When build:
      """
      jFactory.type(Bean.class).property("values[1]", false).property("values[3]", false).create();
      """
      Then the result should:
      """
      values: [true, false]
      """

    Scenario Outline: Date list with more than one elements value specified
      Given the following bean class:
      """
      public class Bean {
          public <type> values;
      }
      """
      When build:
      """
      jFactory.type(Bean.class).property("values[1]", "2023-06-25").property("values[3]", "2023-07-08").create();
      """
      Then the result should:
      """
      : {
        values[0].toInstant: "1996-01-24T00:00:00Z"
        values[1].toInstant: "2023-06-25T00:00:00Z"
        values[2].toInstant: "1996-01-24T00:00:00Z"
        values[3].toInstant: "2023-07-08T00:00:00Z"
      }
      """
      Examples:
        | type       |
        | Date[]     |
        | List<Date> |

    Scenario: Date Set with more than one elements value specified
      Given the following bean class:
        """
        public class Bean {
            public Set<Date> values;
        }
        """
      When build:
        """
        jFactory.type(Bean.class).property("values[1]", "2023-06-25").property("values[3]", "2023-07-08").create();
        """
      Then the result should:
        """
        : {
          values[0].toInstant: "1996-01-24T00:00:00Z"
          values[1].toInstant: "2023-06-25T00:00:00Z"
          values[2].toInstant: "2023-07-08T00:00:00Z"
        }
        """

    Scenario Outline: OffsetDateTime/ZonedDateTime list with more than one elements value specified
      Given the following bean class:
      """
      public class Bean {
          public <type> values;
      }
      """
      When build:
      """
      jFactory.type(Bean.class).property("values[1]", "2023-06-25T06:48:03Z").property("values[3]", "2023-07-08T15:26:03Z").create();
      """
      Then the result should:
      """
      : {
        values[0].toInstant: "1996-01-23T00:00:01Z"
        values[1].toInstant: "2023-06-25T06:48:03Z"
        values[2].toInstant: "1996-01-23T00:00:01Z"
        values[3].toInstant: "2023-07-08T15:26:03Z"
      }
      """
      Examples:
        | type                           |
        | java.time.OffsetDateTime[]     |
        | List<java.time.OffsetDateTime> |
        | java.time.ZonedDateTime[]      |
        | List<java.time.ZonedDateTime>  |

    Scenario Outline: OffsetDateTime/ZoneDateTime Set with more than one elements value specified
      Given the following bean class:
      """
      public class Bean {
          public Set<<type>> values;
      }
      """
      When build:
      """
      jFactory.type(Bean.class).property("values[1]", "2023-06-25T06:48:03Z").property("values[3]", "2023-07-08T15:26:03Z").create();
      """
      Then the result should:
      """
      : {
        values[0].toInstant: "1996-01-23T00:00:01Z"
        values[1].toInstant: "2023-06-25T06:48:03Z"
        values[2].toInstant: "2023-07-08T15:26:03Z"
      }
      """
      Examples:
        | type                     |
        | java.time.OffsetDateTime |
        | java.time.ZonedDateTime  |

    Scenario Outline: Enum list with more than one elements value specified
      Given the following bean class:
      """
      public class Bean {
          public <type> values;
          public enum EnumType {
            A, B
          }
      }
      """
      When build:
      """
      jFactory.type(Bean.class).property("values[1]", "B").property("values[3]", "B").create();
      """
      Then the result should:
      """
      values: [A, B, A, B]
      """
      Examples:
        | type           |
        | EnumType[]     |
        | List<EnumType> |

    Scenario: Enum Set with more than one elements value specified
      Given the following bean class:
      """
      public class Bean {
          public Set<EnumType> values;
          public enum EnumType {
            A, B
          }
      }
      """
      When build:
      """
      jFactory.type(Bean.class).property("values[1]", "B").property("values[3]", "B").create();
      """
      Then the result should:
      """
      values: [A, B]
      """

  Rule: system default value for basic type

    Scenario: system default value for String
      When build:
      """
      jFactory.type(String.class).create();
      """
      Then the result should:
      """
      = ''
      """

    Scenario: first system default value for Date
      When build:
      """
      jFactory.type(Date.class).create();
      """
      Then the result should:
      """
      toInstant: '1970-01-01T00:00:00.001Z'
      """
      When build:
      """
      jFactory.type(Date.class).create();
      """
      Then the result should:
      """
      toInstant: '1970-01-01T00:00:00.002Z'
      """

#    Scenario: throw exception now - system default value for custom type YearMonth
#      When build:
#      """
#      jFactory.type(java.time.YearMonth.class).create();
#      """
#      Then the result should:
#      """
#      = null
#      """

    Scenario: system default value for any custom type
      Given the following bean class:
      """
      public class AnyCustomObject {
        public String anyProperty;
      }
      """
      When build:
      """
      jFactory.type(AnyCustomObject.class).create();
      """
      Then the result should:
      """
      = {
        anyProperty: anyProperty#1
      }
      """
      When build:
      """
      jFactory.type(AnyCustomObject.class).create();
      """
      Then the result should:
      """
      = {
        anyProperty: anyProperty#2
      }
      """

#    Scenario: throw exception now - system default value for enum
#      Given the following bean class:
#      """
#      public class Bean {
#          public enum EnumType {
#            A, B
#          }
#      }
#      """
#      When build:
#      """
#      jFactory.type(Bean.EnumType.class).create();
#      """
#      Then the result should:
#      """
#      = ''      # now throws exception
#      """
#
#    Scenario Outline: throw exception now - system default value for Integer/Long/Byte/Short/Float/BigInteger/BigDecimal/Boolean/UUID/LocalDate/Instant/LocalTime/LocalDateTime/OffsetDateTime
#      When build:
#      """
#      jFactory.type(<type>.class).create();
#      """
#      Then the result should:
#      """
#      = 1       # now throws exception
#      """
#      Examples:
#        | type                     |
#        | Integer                  |
#        | Long                     |
#        | Byte                     |
#        | Short                    |
#        | Float                    |
#        | BigInteger               |
#        | BigDecimal               |
#        | Boolean                  |
#        | UUID                     |
#        | java.time.LocalDate      |
#        | java.time.Instant        |
#        | java.time.LocalTime      |
#        | java.time.LocalDateTime  |
#        | java.time.OffsetDateTime |

  Rule: system default value for basic type list

    Scenario Outline: String/int/Integer/short/Short/long/Long/byte/Byte/float/Float/double/Double/BigInteger/BigDecimal/boolean/Boolean/UUID/Date/Instant/LocalDate/LocalTime/LocalDateTime/OffsetDateTime/ZonedDateTime/Enum base type list with no element value specified
      Given the following bean class:
      """
      public class Bean {
          public enum EnumType {
            A, B
          }
      }
      """
      When build:
      """
      jFactory.type(<type>.class).create();
      """
      Then the result should:
      """
      = []
      """
      Examples:
        | type                       |
        | String[]                   |
        | int[]                      |
        | Integer[]                  |
        | short[]                    |
        | Short[]                    |
        | long[]                     |
        | Long[]                     |
        | byte[]                     |
        | Byte[]                     |
        | float[]                    |
        | Float[]                    |
        | double[]                   |
        | Double[]                   |
        | BigInteger[]               |
        | BigDecimal[]               |
        | boolean[]                  |
        | Boolean[]                  |
        | UUID[]                     |
        | java.time.Instant[]        |
        | java.time.LocalDate[]      |
        | java.time.LocalTime[]      |
        | java.time.LocalDateTime[]  |
        | java.time.OffsetDateTime[] |
        | java.time.ZonedDateTime[]  |
        | Date[]                     |
        | Bean.EnumType[]            |
        | List                       |
        | Set                        |

    Scenario Outline: Custom base type list with no element value specified
      When build:
      """
      jFactory.type(<type>.class).create();
      """
      Then the result should:
      """
      = []
      """
      Examples:
        | type                  |
        | java.time.YearMonth[] |
        | CustomObject[]        |

    Scenario: list with one element specified
      When build:
      """
      jFactory.type(List.class).property("[1]", "hello").create();
      """
      Then the result should:
      """
      = [null, hello]
      """

#    Scenario: throw exception now - set with one element specified
#      When build:
#      """
#      jFactory.type(Set.class).property("[1]", "hello").create();
#      """
#      Then the result should:
#      """
#      = [null, hello]       # now throw exception
#      """

    Scenario: list with more than one element specified
      When build:
      """
      jFactory.type(List.class).property("[1]", "hello").property("[3]", "world").create();
      """
      Then the result should:
      """
      = [null, hello, null, world]
      """

    Scenario Outline: String/Integer/Long/Short/Byte/Float/Double/BigInteger/BigDecimal/Boolean/UUID/LocalDate/LocalTime/Instant/LocalDateTime/OffsetDateTime/Enum basic type array with one element specified
      Given the following bean class:
      """
      public class Bean {
          public enum EnumType {
            A, B
          }
      }
      """
      When build:
      """
      jFactory.type(<type>[].class).property("[1]", <specifiedValue>).create();
      """
      Then the result should:
      """
      : [<defaultValue>, <specifiedValue>]
      """
      Examples:
        | type                     | specifiedValue                         | defaultValue                           |
        | String                   | "hello"                                | "0#1"                                  |
        | Integer                  | 42                                     | 1                                      |
        | Long                     | 42                                     | 1                                      |
        | Short                    | 42                                     | 1                                      |
        | Byte                     | 42                                     | 1                                      |
        | Float                    | 42.0f                                  | 1.0f                                   |
        | Double                   | 42.0d                                  | 1.0d                                   |
        | BigInteger               | 42                                     | 1                                      |
        | BigDecimal               | 42.0                                   | 1.0                                    |
        | Boolean                  | true                                   | true                                   |
        | UUID                     | "5abbd538-3ef6-4391-8e4f-6dc9f98ac505" | "00000000-0000-0000-0000-000000000001" |
        | java.time.LocalDate      | "2023-06-25"                           | "1996-01-24"                           |
        | java.time.LocalTime      | "08:48:03"                             | "00:00:01"                             |
        | java.time.Instant        | "2023-06-25T08:48:03Z"                 | "1996-01-23T00:00:01Z"                 |
        | java.time.LocalDateTime  | "2023-06-25T08:48:03"                  | "1996-01-23T00:00:01"                  |
        | java.time.OffsetDateTime | "2023-06-25T08:48:03Z"                 | "1996-01-23T00:00:01Z"                 |
        | Bean.EnumType            | "B"                                    | "A"                                    |

    Scenario Outline: Custom basic type array with one element specified
      When build:
      """
      jFactory.type(<type>[].class).property("[1]", <specifiedValue>).create();
      """
      Then the result should:
      """
      : [<defaultValue>, <expectedSpecifiedValue>]
      """
      Examples:
        | type                | specifiedValue     | defaultValue | expectedSpecifiedValue |
        | java.time.YearMonth | "2023-06"          | null         | '2023-06'              |
        | CustomObject        | new CustomObject() | null         | { anyProperty= null }  |

    Scenario: Date basic type array with one element specified
      When build:
      """
      jFactory.type(Date[].class).property("[1]", "2023-06-25").create();
      """
      Then the result should:
      """
      : {
        0.toInstant: "1996-01-24T00:00:00Z"
        1.toInstant: "2023-06-25T00:00:00Z"
      }
      """

#    Scenario Outline: throw exception now - int/short/long/byte/float/double/boolean basic type array with one element specified
#      When build:
#      """
#      jFactory.type(<type>[].class).property("[1]", <specifiedValue>).create();
#      """
#      Then the result should:
#      """
#      = [<defaultValue>, <specifiedValue>]      # now throw exception
#      """
#      Examples:
#        | type    | specifiedValue | defaultValue |
#        | int     | 42             | 1            |
#        | short   | 42             | 1            |
#        | long    | 42             | 1            |
#        | byte    | 42             | 1            |
#        | float   | 42.0f          | 1.0f         |
#        | double  | 42.0d          | 1.0d         |
#        | boolean | true           | true         |

    Scenario Outline: String/Integer/Long/Short/Byte/Float/Double/BigInteger/BigDecimal/Boolean/UUID/LocalDate/LocalTime/Instant/LocalDateTime/OffsetDateTime/Enum basic type array with more than one element specified
      Given the following bean class:
      """
      public class Bean {
          public enum EnumType {
            A, B
          }
      }
      """
      When build:
      """
      jFactory.type(<type>[].class).property("[1]", <firstSpecifiedValue>).property("[3]", <secondSpecifiedValue>).create();
      """
      Then the result should:
      """
      : [<firstDefaultValue>, <firstSpecifiedValue>, <secondDefaultValue>, <secondSpecifiedValue>]
      """
      Examples:
        | type                     | firstSpecifiedValue                    | secondSpecifiedValue                   | firstDefaultValue                      | secondDefaultValue                     |
        | String                   | "hello"                                | "world"                                | "0#1"                                  | "2#1"                                  |
        | Integer                  | 42                                     | 12306                                  | 1                                      | 1                                      |
        | Long                     | 42                                     | 12306                                  | 1                                      | 1                                      |
        | Short                    | 42                                     | 12306                                  | 1                                      | 1                                      |
        | Byte                     | 42                                     | 75                                     | 1                                      | 1                                      |
        | Float                    | 42.0                                   | 75.0                                   | 1.0                                    | 1.0                                    |
        | Double                   | 42.0                                   | 75.0                                   | 1.0                                    | 1.0                                    |
        | BigInteger               | 42                                     | 75                                     | 1                                      | 1                                      |
        | BigDecimal               | 42.0                                   | 75.0                                   | 1.0                                    | 1.0                                    |
        | Boolean                  | true                                   | false                                  | true                                   | true                                   |
        | UUID                     | "5b5e9230-3b4e-4b6e-8f0d-0b9e8e0e6c6d" | "7f6e5d4c-3b2a-1b0c-9a8b-7c6d5e4f3a2b" | "00000000-0000-0000-0000-000000000001" | "00000000-0000-0000-0000-000000000001" |
        | java.time.LocalDate      | "2023-06-25"                           | "2023-07-08"                           | "1996-01-24"                           | "1996-01-24"                           |
        | java.time.LocalTime      | "08:48:03"                             | "17:26:03"                             | "00:00:01"                             | "00:00:01"                             |
        | java.time.Instant        | "2023-06-25T08:48:03Z"                 | "2023-07-08T08:48:03Z"                 | "1996-01-23T00:00:01Z"                 | "1996-01-23T00:00:01Z"                 |
        | java.time.LocalDateTime  | "2023-06-25T08:48:03"                  | "2023-07-08T17:26:03"                  | "1996-01-23T00:00:01"                  | "1996-01-23T00:00:01"                  |
        | java.time.OffsetDateTime | "2023-06-25T08:48:03Z"                 | "2023-07-08T15:26:03Z"                 | "1996-01-23T00:00:01Z"                 | "1996-01-23T00:00:01Z"                 |
        | Bean.EnumType            | "B"                                    | "A"                                    | "A"                                    | "A"                                    |

    Scenario Outline: Custom basic type array with more than one element specified
      When build:
      """
      jFactory.type(<type>[].class).property("[1]", <firstSpecifiedValue>).property("[3]", <secondSpecifiedValue>).create();
      """
      Then the result should:
      """
      : [<firstDefaultValue>, <expectedFirstSpecifiedValue>, <secondDefaultValue>, <expectedSecondSpecifiedValue>]
      """
      Examples:
        | type                | firstSpecifiedValue | secondSpecifiedValue | firstDefaultValue | secondDefaultValue | expectedFirstSpecifiedValue | expectedSecondSpecifiedValue |
        | java.time.YearMonth | "2023-06"           | "2023-07"            | null              | null               | '2023-06'                   | '2023-07'                    |
        | CustomObject        | new CustomObject()  | new CustomObject()   | null              | null               | { anyProperty= null }       | { anyProperty= null }        |

    Scenario: Date basic type array with more than one element specified
      When build:
      """
      jFactory.type(Date[].class).property("[1]", "2023-06-25").property("[3]", "2023-07-08").create();
      """
      Then the result should:
      """
      : {
        0.toInstant: "1996-01-24T00:00:00Z"
        1.toInstant: "2023-06-25T00:00:00Z"
        2.toInstant: "1996-01-24T00:00:00Z"
        3.toInstant: "2023-07-08T00:00:00Z"
      }
      """

  Rule: Ignore default value

    Background:
      Given the following bean class:
      """
      public class BeanOne {
        public String stringValue;
        public int intValue;
        public Integer integerValue;
        public short shortValue;
        public Short shortClassValue;
        public byte byteValue;
        public Byte byteClassValue;
        public long longValue;
        public Long longClassValue;
        public float floatValue;
        public Float floatClassValue;
        public double doubleValue;
        public Double doubleClassValue;
        public boolean booleanValue;
        public Boolean booleanClassValue;
        public BigInteger bigIntegerValue;
        public BigDecimal bigDecimalValue;
        public UUID uuidValue;
        public Date dateValue;
        public java.time.Instant instantValue;
        public java.time.LocalDate localDateValue;
        public java.time.LocalTime localTimeValue;
        public java.time.LocalDateTime localDateTimeValue;
        public java.time.OffsetDateTime offsetDateTimeValue;
        public java.time.ZonedDateTime zonedDateTimeValue;
        public EnumType enumValue;

        public enum EnumType {
          A, B
        }
      }
      """
      And the following bean class:
      """
      public class BeanTwo {
        public String stringValue;
        public int intValue;
        public Integer integerValue;
        public short shortValue;
        public Short shortClassValue;
        public byte byteValue;
        public Byte byteClassValue;
        public long longValue;
        public Long longClassValue;
        public float floatValue;
        public Float floatClassValue;
        public double doubleValue;
        public Double doubleClassValue;
        public boolean booleanValue;
        public Boolean booleanClassValue;
        public BigInteger bigIntegerValue;
        public BigDecimal bigDecimalValue;
        public UUID uuidValue;
        public Date dateValue;
        public java.time.Instant instantValue;
        public java.time.LocalDate localDateValue;
        public java.time.LocalTime localTimeValue;
        public java.time.LocalDateTime localDateTimeValue;
        public java.time.OffsetDateTime offsetDateTimeValue;
        public java.time.ZonedDateTime zonedDateTimeValue;
        public EnumType enumValue;

        public enum EnumType {
          A, B
        }
      }
      """

    Scenario: Ignore default value for all bean property
      When register:
      """
      jFactory.ignoreDefaultValue(propertyWriter -> Arrays.asList("stringValue", "intValue", "integerValue", "shortValue", "shortClassValue", "byteValue", "byteClassValue", "longValue", "longClassValue", "floatValue", "floatClassValue", "doubleValue", "doubleClassValue", "booleanValue", "booleanClassValue", "bigIntegerValue", "bigDecimalValue", "uuidValue", "dateValue", "instantValue", "localDateValue", "localTimeValue", "localDateTimeValue", "offsetDateTimeValue", "zonedDateTimeValue", "enumValue")
        .contains(propertyWriter.getName()));
      """
      And build:
      """
      jFactory.type(BeanOne.class).create();
      """
      Then the result should:
      """
      : {
        stringValue= null
        intValue= 0
        integerValue= null
        shortValue= 0s
        shortClassValue= null
        byteValue= 0y
        byteClassValue= null
        longValue= 0l
        longClassValue= null
        floatValue= 0.0f
        floatClassValue= null
        doubleValue= 0.0d
        doubleClassValue= null
        booleanValue= false
        booleanClassValue= null
        bigIntegerValue= null
        bigDecimalValue= null
        uuidValue= null
        dateValue= null
        instantValue= null
        localDateValue= null
        localTimeValue= null
        localDateTimeValue= null
        offsetDateTimeValue= null
        zonedDateTimeValue= null
        enumValue= null
      }
      """
      And build:
      """
      jFactory.type(BeanTwo.class).create();
      """
      Then the result should:
      """
      : {
        stringValue= null
        intValue= 0
        integerValue= null
        shortValue= 0s
        shortClassValue= null
        byteValue= 0y
        byteClassValue= null
        longValue= 0l
        longClassValue= null
        floatValue= 0.0f
        floatClassValue= null
        doubleValue= 0.0d
        doubleClassValue= null
        booleanValue= false
        booleanClassValue= null
        bigIntegerValue= null
        bigDecimalValue= null
        uuidValue= null
        dateValue= null
        instantValue= null
        localDateValue= null
        localTimeValue= null
        localDateTimeValue= null
        offsetDateTimeValue= null
        zonedDateTimeValue= null
        enumValue= null
      }
      """

    Scenario: ignore property on spec
      Given the following spec class:
      """
      public class BeanSpecOne extends Spec<BeanOne> {

        @Override
        public void main() {
          property("stringValue").ignore();
          property("intValue").ignore();
          property("integerValue").ignore();
          property("shortValue").ignore();
          property("shortClassValue").ignore();
          property("byteValue").ignore();
          property("byteClassValue").ignore();
          property("longValue").ignore();
          property("longClassValue").ignore();
          property("floatValue").ignore();
          property("floatClassValue").ignore();
          property("doubleValue").ignore();
          property("doubleClassValue").ignore();
          property("booleanValue").ignore();
          property("booleanClassValue").ignore();
          property("bigIntegerValue").ignore();
          property("bigDecimalValue").ignore();
          property("uuidValue").ignore();
          property("dateValue").ignore();
          property("instantValue").ignore();
          property("localDateValue").ignore();
          property("localTimeValue").ignore();
          property("localDateTimeValue").ignore();
          property("offsetDateTimeValue").ignore();
          property("zonedDateTimeValue").ignore();
          property("enumValue").ignore();
        }

      }
      """
      And the following spec class:
      """
      public class BeanSpecTwo extends Spec<BeanTwo> {
      }
      """
      When build:
      """
      jFactory.spec(BeanSpecOne.class).create();
      """
      Then the result should:
      """
      : {
        stringValue= null
        intValue= 0
        integerValue= null
        shortValue= 0s
        shortClassValue= null
        byteValue= 0y
        byteClassValue= null
        longValue= 0l
        longClassValue= null
        floatValue= 0.0f
        floatClassValue= null
        doubleValue= 0.0d
        doubleClassValue= null
        booleanValue= false
        booleanClassValue= null
        bigIntegerValue= null
        bigDecimalValue= null
        uuidValue= null
        dateValue= null
        instantValue= null
        localDateValue= null
        localTimeValue= null
        localDateTimeValue= null
        offsetDateTimeValue= null
        zonedDateTimeValue= null
        enumValue= null
      }
      """
      When build:
      """
      jFactory.spec(BeanSpecTwo.class).create();
      """
      When the result should:
      """
      : {
        stringValue= stringValue#1
        intValue= 1
        integerValue= 1
        shortValue= 1s
        shortClassValue= 1s
        byteValue= 1y
        byteClassValue= 1y
        longValue= 1l
        longClassValue= 1l
        floatValue= 1.0f
        floatClassValue= 1.0f
        doubleValue= 1.0d
        doubleClassValue= 1.0d
        booleanValue= true
        booleanClassValue= true
        bigIntegerValue= 1bi
        bigDecimalValue= 1bd
        uuidValue: '00000000-0000-0000-0000-000000000001'
        dateValue.toInstant: '1996-01-24T00:00:00Z'
        instantValue: '1996-01-23T00:00:01Z'
        localDateValue: '1996-01-24'
        localTimeValue: '00:00:01'
        localDateTimeValue: '1996-01-23T00:00:01'
        offsetDateTimeValue.toInstant: '1996-01-23T00:00:01Z'
        zonedDateTimeValue.toInstant: '1996-01-23T00:00:01Z'
        enumValue: A
      }
      """

    Scenario: ignore property with in one method on spec
      Given the following spec class:
      """
      public class BeanSpecOne extends Spec<BeanOne> {

        @Override
        public void main() {
          ignore("stringValue", "intValue");
        }
      }
      """
      And the following spec class:
      """
      public class BeanSpecTwo extends Spec<BeanTwo> {
      }
      """
      When build:
      """
      jFactory.spec(BeanSpecOne.class).create();
      """
      Then the result should:
      """
      : {
        stringValue= null
        intValue= 0
      }
      """
