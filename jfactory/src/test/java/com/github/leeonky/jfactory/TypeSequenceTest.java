package com.github.leeonky.jfactory;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TypeSequenceTest {
    TypeSequence typeSequence = new TypeSequence();

    @Test
    void generate_sequence_by_type() {
        TypeSequence.Sequence sequence = typeSequence.register(Integer.class);

        assertThat(sequence.get()).isEqualTo(1);
        assertThat(sequence.get()).isEqualTo(1);
    }

    @Test
    void only_increment_after_get() {
        TypeSequence.Sequence sequence1 = typeSequence.register(Integer.class);
        TypeSequence.Sequence sequence2 = typeSequence.register(Integer.class);

        assertThat(sequence2.get()).isEqualTo(1);
        assertThat(sequence1.get()).isEqualTo(2);

        assertThat(sequence2.get()).isEqualTo(1);
        assertThat(sequence1.get()).isEqualTo(2);
    }
}