package com.github.leeonky.jfactory;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class CompositeDataRepositoryTest {
    CompositeDataRepository compositeDataRepository = new CompositeDataRepository();

    @Test
    void raise_error_when_no_default() {
        assertThat(Assertions.assertThrows(IllegalStateException.class, () ->
                compositeDataRepository.save(new Object())).getMessage()).isEqualTo("There is no default or appropriate repository available for type `java.lang.Object`");

        assertThat(Assertions.assertThrows(IllegalStateException.class, () ->
                compositeDataRepository.queryAll(Object.class)).getMessage()).isEqualTo("There is no default or appropriate repository available for type `java.lang.Object`");
    }

    @Test
    void do_nothing_when_save_null() {
        try {
            compositeDataRepository.save(null);
        } catch (Throwable e) {
            fail("should not be here");
        }
    }

    @Nested
    class DefaultRepo {
        DataRepository defaultRepo = Mockito.mock(DataRepository.class);

        @BeforeEach
        void mock() {
            compositeDataRepository = new CompositeDataRepository(defaultRepo);
        }

        @Test
        void support_default_repo() {
            Object object = new Object();
            compositeDataRepository.save(object);

            verify(defaultRepo).save(object);

            compositeDataRepository.queryAll(Object.class);
            verify(defaultRepo).queryAll(Object.class);
        }

        @Test
        void should_clear_default_repo_when_clear() {
            compositeDataRepository.clear();

            verify(defaultRepo).clear();
        }
    }

    @Nested
    class RegisterSub {
        DataRepository defaultRepo = Mockito.mock(DataRepository.class);
        DataRepository registeredRepo = Mockito.mock(DataRepository.class);

        @BeforeEach
        void mock() {
            compositeDataRepository = new CompositeDataRepository(defaultRepo);
        }

        @Test
        void register_by_type() {
            compositeDataRepository.registerByType(Object.class, registeredRepo);

            Object object = new Object();
            compositeDataRepository.save(object);

            verify(registeredRepo).save(object);
            verify(defaultRepo, never()).save(any(Object.class));

            compositeDataRepository.queryAll(Object.class);
            verify(registeredRepo).queryAll(Object.class);
            verify(defaultRepo, never()).queryAll(any(Class.class));

        }

        @Test
        void register_by_package() {
            compositeDataRepository.registerByPackage("java.lang", registeredRepo);

            Object object = new Object();
            compositeDataRepository.save(object);

            verify(registeredRepo).save(object);
            verify(defaultRepo, never()).save(any(Object.class));

            compositeDataRepository.queryAll(Object.class);
            verify(registeredRepo).queryAll(Object.class);
            verify(defaultRepo, never()).queryAll(any(Class.class));
        }

        @Test
        void register_by_lambda() {
            compositeDataRepository.registerBy(type -> type.equals(Object.class), registeredRepo);

            Object object = new Object();
            compositeDataRepository.save(object);

            verify(registeredRepo).save(object);
            verify(defaultRepo, never()).save(any(Object.class));

            compositeDataRepository.queryAll(Object.class);
            verify(registeredRepo).queryAll(Object.class);
            verify(defaultRepo, never()).queryAll(any(Class.class));
        }

        @Test
        void later_registrations_have_higher_priority() {
            DataRepository later = Mockito.mock(DataRepository.class);
            compositeDataRepository.registerByType(Object.class, registeredRepo);
            compositeDataRepository.registerBy(type -> type.equals(Object.class), later);

            Object object = new Object();
            compositeDataRepository.save(object);

            verify(later).save(object);
            verify(registeredRepo, never()).save(any(Object.class));
            verify(defaultRepo, never()).save(any(Object.class));

            compositeDataRepository.queryAll(Object.class);
            verify(later).queryAll(Object.class);
            verify(registeredRepo, never()).queryAll(any(Class.class));
            verify(defaultRepo, never()).queryAll(any(Class.class));
        }

        @Test
        void should_clear_all_sub_repo_when_clear() {
            DataRepository later = Mockito.mock(DataRepository.class);
            compositeDataRepository.registerByType(Object.class, registeredRepo);
            compositeDataRepository.registerBy(type -> type.equals(Object.class), later);

            compositeDataRepository.clear();

            verify(later).clear();
            verify(registeredRepo).clear();
            verify(defaultRepo).clear();
        }
    }
}