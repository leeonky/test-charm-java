package com.github.leeonky.dal.runtime;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DalRuntimeExceptionTest {

    @Test
    void no_message_no_cause_show_type() {
        assertThat(new DalRuntimeException().getMessage()).isEqualTo("com.github.leeonky.dal.runtime.DalRuntimeException");
    }

    @Test
    void has_message_no_cause_show_message() {
        assertThat(new DalRuntimeException("hello").getMessage()).isEqualTo("hello");
    }

    @Test
    void no_message_has_cause_show_cause_message() {
        assertThat(new DalRuntimeException(new Exception("hello")).getMessage()).isEqualTo("hello");
    }

    @Test
    void has_message_and_cause_show_message_first_and_show_cause_type_and_message_in_new_line() {
        assertThat(new DalRuntimeException("error", new Exception("hello")).getMessage()).isEqualTo("error\nhello");
    }
}