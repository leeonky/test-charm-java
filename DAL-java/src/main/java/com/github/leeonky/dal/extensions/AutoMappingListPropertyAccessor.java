package com.github.leeonky.dal.extensions;

import com.github.leeonky.dal.runtime.AutoMappingList;
import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.runtime.JavaClassPropertyAccessor;
import com.github.leeonky.util.BeanClass;

class AutoMappingListPropertyAccessor extends JavaClassPropertyAccessor<AutoMappingList> {
    public AutoMappingListPropertyAccessor() {
        super(BeanClass.create(AutoMappingList.class));
    }

    @Override
    public Object getValueByData(Data.Resolved data, Object property) {
        return data.list().autoMapping(item -> item.getValue(property));
    }
}
