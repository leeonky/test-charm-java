package com.github.leeonky.jfactory.spec.core;

import com.github.leeonky.jfactory.DataRepository;
import com.github.leeonky.jfactory.JFactory;
import org.junit.jupiter.api.Test;

import java.util.*;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

class RepositorySave {

    @Test
    void save_object_after_create() {
        List<Object> beans = new ArrayList<>();

        JFactory jFactory = new JFactory(new DataRepository() {
            @Override
            public <T> Collection<T> queryAll(Class<T> type) {
                return Collections.emptyList();
            }

            @Override
            public void clear() {
            }

            @Override
            public void save(Object object) {
                beans.add(object);
            }
        });

        Bean bean = jFactory.type(Bean.class).create();

        assertThat(beans).containsExactly(bean);
    }

    public static class Bean {
    }
}

class RepositoryQueryAll {

    @Test
    void query_all_with_empty_repo() {
        JFactory jFactory = new JFactory(new DataRepository() {
            @Override
            public <T> Collection<T> queryAll(Class<T> type) {
                return Collections.emptyList();
            }

            @Override
            public void clear() {
            }

            @Override
            public void save(Object object) {
            }
        });

        assertThat(jFactory.type(Bean.class).queryAll()).isEmpty();
    }

    @Test
    void query_all_from_repo() {
        Bean bean = new Bean();
        JFactory jFactory = new JFactory(new DataRepository() {
            @Override
            public <T> Collection<T> queryAll(Class<T> type) {
                return (Collection<T>) asList(bean);
            }

            @Override
            public void clear() {
            }

            @Override
            public void save(Object object) {
            }
        });

        assertThat(jFactory.type(Bean.class).queryAll()).containsExactly(bean);
    }

    @Test
    void query_all_from_repo_with_criteria() {
        Bean bean1 = new Bean();
        bean1.str1 = "hello";
        Bean bean2 = new Bean();
        bean2.str1 = "world";

        JFactory jFactory = new JFactory(new DataRepository() {
            @Override
            public <T> Collection<T> queryAll(Class<T> type) {
                return (Collection<T>) asList(bean1, bean2);
            }

            @Override
            public void clear() {
            }

            @Override
            public void save(Object object) {
            }
        });

        assertThat(jFactory.type(Bean.class).property("str1", "hello").queryAll()).containsExactly(bean1);
    }

    @Test
    void query_all_from_repo_with_multiple_criteria() {
        Bean bean1 = new Bean();
        bean1.str1 = "hello";
        bean1.str2 = "not-matched";
        Bean bean2 = new Bean();
        bean2.str1 = "hello";
        bean2.str2 = "world";

        JFactory jFactory = new JFactory(new DataRepository() {
            @Override
            public <T> Collection<T> queryAll(Class<T> type) {
                return (Collection<T>) asList(bean1, bean2);
            }

            @Override
            public void clear() {
            }

            @Override
            public void save(Object object) {
            }
        });

        assertThat(jFactory.type(Bean.class).properties(new HashMap<String, Object>() {{
            put("str1", "hello");
            put("str2", "world");
        }}).queryAll()).containsExactly(bean2);
    }

    public static class Bean {
        public String str1, str2;
    }
}