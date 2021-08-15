package com.github.leeonky.dal.spec;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

class VerifyObject extends Base {

    @Nested
    class EqualTo {

        @Test
        void equal_to_empty_object() {
            assertPass(new HashMap<>(), "= {}");
        }

        @Test
        void raise_error_when_has_unexpected_field() {
            assertFailed(new HashMap<String, Object>() {{
                put("key", 1);
            }}, "= {}");
        }

        @Test
        void object_key_sets_should_be_equal_and_each_property_should_pass_then_return_pass() {
            assertPass(new HashMap<String, Object>() {{
                put("key", 1);
            }}, "= {key: 1}");
        }

        @Test
        void return_false_when_object_key_sets_not_equal() {
            assertFailed(new HashMap<String, Object>() {{
                put("key", 1);
            }}, "= {}");
        }

        @Test
        void return_false_when_any_field_value_not_matches() {
            assertFailed(new HashMap<String, Object>() {{
                put("key1", '1');
                put("key2", '2');
            }}, "= {key1: '1' key2: 'not match'}");
        }
    }

    @Nested
    class Matches {

        @Test
        void any_non_null_object_matches_empty_object() {
            assertPass(new HashMap<>(), ": {}");
            assertPass(new HashMap<String, Object>() {{
                put("any field", "any value");
            }}, ": {}");
            assertPass(1, ": {}");
        }

        @Test
        void null_does_not_match_empty_object() {
            assertFailed(null, ": {}");
        }

        @Test
        void should_only_verify_expected_key_values_in_given_object() {
            assertPass(new HashMap<String, Object>() {{
                put("key", 1);
                put("another key", 2);
            }}, ": {key: 1}");
        }

        @Test
        void return_false_when_any_field_verification_failed() {
            assertFailed(new HashMap<String, Object>() {{
                put("key1", '1');
                put("key2", '2');
                put("key3", '3');
            }}, ": {key2: 'not match'}");
        }
    }

    @Nested
    class NestedObject {

        @Test
        void support_match_nested_object() {
            assertPass(new HashMap<String, Object>() {{
                put("key1", 1);
                put("key2", new HashMap<String, Object>() {{
                    put("skey1", 3);
                    put("skey2", 4);
                }});
            }}, "= {key1: 1 key2= {skey1: 3 skey2: 4}}");


            assertFailed(new HashMap<String, Object>() {{
                put("key1", 1);
                put("key2", new HashMap<String, Object>() {{
                    put("skey1", 3);
                    put("skey2", 4);
                }});
            }}, "= {key2= {skey1: 3 skey2: 4}}");

            assertFailed(new HashMap<String, Object>() {{
                put("key1", 1);
                put("key2", new HashMap<String, Object>() {{
                    put("skey1", 3);
                    put("skey2", 4);
                }});
            }}, "= {key1: 1 key2= {skey1: 3 skey2: 10000}}");
        }

        @Test
        void support_property_chain() {
            assertPass(new HashMap<String, Object>() {{
                put("key1", new HashMap<String, Object>() {{
                    put("s1", 3);
                    put("s2", 4);
                }});
                put("key2", 2);
            }}, "= {key1.s1: 3 key1.s2: 4 key2: 2}");

            assertFailed(new HashMap<String, Object>() {{
                put("key1", new HashMap<String, Object>() {{
                    put("s1", 3);
                }});
                put("key2", 2);
            }}, "= {key1.s1: 3}");

            assertFailed(new HashMap<String, Object>() {{
                put("key1", new HashMap<String, Object>() {{
                    put("s1", 3);
                }});
                put("key2", 2);
            }}, "= {key1.s1: 2 key2: 2}");
        }
    }

    //TODO property
    //TODO nested object
    //TODO property chain
    //TODO process getClass property for java bean and size property of list
    //TODO property is alias
    //TODO sub alias
}
