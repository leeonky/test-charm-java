package com.github.leeonky.dal.runtime;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DALRuntimeExceptionTest {

    @Test
    void no_message_no_cause_show_type() {
        assertThat(new DALRuntimeException().getMessage()).isEqualTo("com.github.leeonky.dal.runtime.DALRuntimeException");
    }

    @Test
    void has_message_no_cause_show_message() {
        assertThat(new DALRuntimeException("hello").getMessage()).isEqualTo("hello");
    }

    @Test
    void no_message_has_cause_show_cause_message() {
        assertThat(new DALRuntimeException(new Exception("hello")).getMessage()).isEqualTo("java.lang.Exception: hello");
    }

    @Test
    void has_message_and_cause_show_message_first_and_show_cause_type_and_message_in_new_line() {
        assertThat(new DALRuntimeException("error", new Exception("hello")).getMessage()).isEqualTo("error\njava.lang.Exception: hello");
    }
}