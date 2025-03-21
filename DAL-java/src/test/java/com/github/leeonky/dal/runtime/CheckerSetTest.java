package com.github.leeonky.dal.runtime;

import com.github.leeonky.dal.runtime.checker.Checker;
import com.github.leeonky.dal.runtime.checker.CheckerSet;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class CheckerSetTest {
    private final CheckerSet checkerSet = new CheckerSet(null);
    private final Checker checker = mock(Checker.class);

    @Test
    void checker_for_value_and_value_has_higher_priority() {
        checkerSet.register((d1, d2) -> of(checker));
        checkerSet.register(String.class, String.class, this::failed);
        checkerSet.register(String.class, this::failed);

        assertThat(checkerSet.fetch(data("any"), data("any"))).isEqualTo(checker);
    }

    private Data data(Object obj) {
        return new RuntimeContextBuilder().build(obj).getThis();
    }

    @Test
    void checker_for_type_and_type_has_higher_priority() {
        checkerSet.register((d1, d2) -> empty());
        checkerSet.register(String.class, String.class, (d1, d2) -> of(checker));
        checkerSet.register(String.class, this::failed);

        assertThat(checkerSet.fetch(data("string"), data("string"))).isEqualTo(checker);
    }

    @Test
    void checker_for_type_has_higher_priority() {
        checkerSet.register((d1, d2) -> empty());
        checkerSet.register(String.class, String.class, (d1, d2) -> empty());
        checkerSet.register(String.class, (d1, d2) -> of(checker));

        assertThat(checkerSet.fetch(data("string"), data("string"))).isEqualTo(checker);
    }

    private Optional<Checker> failed(Data d1, Data d2) {
        throw new IllegalStateException();
    }
}