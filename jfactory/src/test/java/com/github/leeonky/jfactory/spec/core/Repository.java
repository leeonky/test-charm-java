package com.github.leeonky.jfactory.spec.core;

import com.github.leeonky.jfactory.DataRepository;
import com.github.leeonky.jfactory.JFactory;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
