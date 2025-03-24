package com.github.leeonky.dal.extensions.inspector.cucumber.pagebk;

import com.github.leeonky.util.BeanClass;

public interface SingleSetter<T> {
    void assign(T value);

    @SuppressWarnings("unchecked")
    default Class<T> getType() {
        return (Class<T>) BeanClass.create(getClass()).getSuper(SingleSetter.class).getTypeArguments(0)
                .orElseThrow(() -> new IllegalStateException("Cannot guess type via generic type argument, please override SingleAssignable::getType"))
                .getType();
    }
}
