package org.testcharm.dal.spec;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.testcharm.dal.format.Formatters;
import org.testcharm.dal.type.Schema;
import org.testcharm.message.MessageConverter;
import org.testcharm.message.MessageConverterRegistry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

class VerifySchemaFormatterField extends Base {
    private MessageConverter jsonConverter = MessageConverterRegistry.jsonConverter();

    public enum E {
        A, B
    }

    public static class InstantNowValue implements Schema {
        public final Formatters.Instant instant = Formatters.Instant.now();
    }

    public static class IntegerValue implements Schema {
        public final Formatters.Integer integer1 = Formatters.Integer.equalTo(1);
        public Formatters.Integer integer2 = Formatters.Integer.equalTo(2);
    }

    public static class IntegerListValue implements Schema {
        public List<Formatters.Integer> integerList = asList(Formatters.Integer.equalTo(1), Formatters.Integer.equalTo(2));
    }

    public static class IntegerArrayValue implements Schema {
        public Formatters.Integer[] integerArray = new Formatters.Integer[]{Formatters.Integer.equalTo(1), Formatters.Integer.equalTo(2)};
    }

    public static class IntegerMapValue implements Schema {
        public Map<String, Formatters.Integer> integerMap = new HashMap<String, Formatters.Integer>() {{
            put("a", Formatters.Integer.equalTo(1));
            put("b", Formatters.Integer.equalTo(2));
        }};
    }

    public static class PositiveIntegerValue implements Schema {
        public Formatters.Integer integer = Formatters.Integer.positive();
    }

    public static class NegativeIntegerValue implements Schema {
        public Formatters.Integer integer = Formatters.Integer.negative();
    }

    public static class NumberValue implements Schema {
        public Formatters.Number number = Formatters.Number.equalTo(1);
    }

    public static class PositiveNumberValue implements Schema {
        public Formatters.Number number = Formatters.Number.positive();
    }

    public static class NegativeNumberValue implements Schema {
        public Formatters.Number number = Formatters.Number.negative();
    }

    @Nested
    class Integer {

        @Test
        void support_verify_integer() {
            dal.getRuntimeContextBuilder().registerSchema(IntegerValue.class);
            assertPass(jsonConverter.deserialize("{" +
                    "\"integer1\": 1," +
                    " \"integer2\": 2" +
                    "}"), "is IntegerValue");

            assertFailed(jsonConverter.deserialize("{" +
                    "\"integer1\": 2," +
                    " \"integer2\": 1" +
                    "}"), "is IntegerValue");
        }

        @Test
        void support_verify_integer_list() {
            dal.getRuntimeContextBuilder().registerSchema(IntegerListValue.class);
            assertPass(jsonConverter.deserialize("{" +
                    "\"integerList\": [1, 2]" +
                    "}"), "is IntegerListValue");

            assertFailed(jsonConverter.deserialize("{" +
                    "\"integerList\": [1]" +
                    "}"), "is IntegerListValue");

            assertFailed(jsonConverter.deserialize("{" +
                    "\"integerList\": [1, 3]" +
                    "}"), "is IntegerListValue");
        }

        @Test
        void support_verify_integer_array() {
            dal.getRuntimeContextBuilder().registerSchema(IntegerArrayValue.class);
            assertPass(jsonConverter.deserialize("{" +
                    "\"integerArray\": [1, 2]" +
                    "}"), "is IntegerArrayValue");

            assertFailed(jsonConverter.deserialize("{" +
                    "\"integerArray\": [1]" +
                    "}"), "is IntegerArrayValue");

            assertFailed(jsonConverter.deserialize("{" +
                    "\"integerArray\": [1, 3]" +
                    "}"), "is IntegerArrayValue");
        }

        @Test
        void support_verify_integer_map() {
            dal.getRuntimeContextBuilder().registerSchema(IntegerMapValue.class);
            assertPass(jsonConverter.deserialize("{" +
                    "\"integerMap\": {\"a\": 1, \"b\": 2}" +
                    "}"), "is IntegerMapValue");

            assertFailed(jsonConverter.deserialize("{" +
                    "\"integerMap\": {\"a\": 1}" +
                    "}"), "is IntegerMapValue");

            assertFailed(jsonConverter.deserialize("{" +
                    "\"integerMap\": {\"a\": 1, \"b\": 3}" +
                    "}"), "is IntegerMapValue");
        }

        @Test
        void support_positive_integer() {
            dal.getRuntimeContextBuilder().registerSchema(PositiveIntegerValue.class);

            assertPass(jsonConverter.deserialize("{" +
                    "\"integer\": 1" +
                    "}"), "is PositiveIntegerValue");

            assertFailed(jsonConverter.deserialize("{" +
                    "\"integer\": 0" +
                    "}"), "is PositiveIntegerValue");

            assertFailed(jsonConverter.deserialize("{" +
                    "\"integer\": -1" +
                    "}"), "is PositiveIntegerValue");
        }

        @Test
        void support_negative_integer() {
            dal.getRuntimeContextBuilder().registerSchema(NegativeIntegerValue.class);

            assertFailed(jsonConverter.deserialize("{" +
                    "\"integer\": 1" +
                    "}"), "is NegativeIntegerValue");

            assertFailed(jsonConverter.deserialize("{" +
                    "\"integer\": 0" +
                    "}"), "is NegativeIntegerValue");

            assertPass(jsonConverter.deserialize("{" +
                    "\"integer\": -1" +
                    "}"), "is NegativeIntegerValue");
        }
    }

    @Nested
    class Instant {

        @Test
        void support_verify_instant_now_value() {
            dal.getRuntimeContextBuilder().registerSchema(InstantNowValue.class);
            assertPass(jsonConverter.deserialize("{\"instant\": \"" + java.time.Instant.now().toString() + "\"}"), "is InstantNowValue");
            assertFailed(jsonConverter.deserialize("{\"instant\": \"" + java.time.Instant.now().plusSeconds(100).toString() + "\"}"), "is InstantNowValue");
        }
    }

    @Nested
    class Number {

        @Test
        void support_equal_to() {
            dal.getRuntimeContextBuilder().registerSchema(NumberValue.class);
            assertPass(jsonConverter.deserialize("{\"number\": 1}"), "is NumberValue");
            assertPass(jsonConverter.deserialize("{\"number\": 1.0}"), "is NumberValue");
            assertFailed(jsonConverter.deserialize("{\"number\": 1.1}"), "is NumberValue");
            assertFailed(jsonConverter.deserialize("{\"number\": \"1\"}"), "is NumberValue");
        }

        @Test
        void support_positive_number() {
            dal.getRuntimeContextBuilder().registerSchema(PositiveNumberValue.class);
            assertPass(jsonConverter.deserialize("{\"number\": 1}"), "is PositiveNumberValue");
            assertPass(jsonConverter.deserialize("{\"number\": 1.0}"), "is PositiveNumberValue");
            assertFailed(jsonConverter.deserialize("{\"number\": 0.0}"), "is PositiveNumberValue");
            assertFailed(jsonConverter.deserialize("{\"number\": 0}"), "is PositiveNumberValue");
            assertFailed(jsonConverter.deserialize("{\"number\": -1}"), "is PositiveNumberValue");
        }

        @Test
        void support_negative_number() {
            dal.getRuntimeContextBuilder().registerSchema(NegativeNumberValue.class);
            assertPass(jsonConverter.deserialize("{\"number\": -1}"), "is NegativeNumberValue");
            assertPass(jsonConverter.deserialize("{\"number\": -1.0}"), "is NegativeNumberValue");
            assertFailed(jsonConverter.deserialize("{\"number\": 0.0}"), "is NegativeNumberValue");
            assertFailed(jsonConverter.deserialize("{\"number\": 0}"), "is NegativeNumberValue");
            assertFailed(jsonConverter.deserialize("{\"number\": 1}"), "is NegativeNumberValue");
        }
    }
}
