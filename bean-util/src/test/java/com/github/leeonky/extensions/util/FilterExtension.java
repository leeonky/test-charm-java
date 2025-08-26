package com.github.leeonky.extensions.util;

import com.github.leeonky.util.AccessorFilter;
import com.github.leeonky.util.AccessorFilterExtension;

public class FilterExtension implements AccessorFilterExtension {
    @Override
    public void extend(AccessorFilter accessorFilter) {
        accessorFilter.exclude(propertyAccessor ->
                propertyAccessor.getName().equals("excludeProperty")
                        && propertyAccessor.getGenericType().equals(int.class));
    }
}
