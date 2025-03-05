package com.github.leeonky.extensions.dal;

import java.util.Set;

//TODO move to DAL
public interface DALObject {
    Object getValue(Object property);

    Set<Object> getPropertyNames();
}
