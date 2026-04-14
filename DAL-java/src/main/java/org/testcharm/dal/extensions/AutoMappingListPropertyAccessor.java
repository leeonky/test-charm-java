package org.testcharm.dal.extensions;

import org.testcharm.dal.runtime.AutoMappingList;
import org.testcharm.dal.runtime.Data;
import org.testcharm.dal.runtime.JavaClassPropertyAccessor;

class AutoMappingListPropertyAccessor extends JavaClassPropertyAccessor<AutoMappingList> {

    @Override
    public Object getValue(Data<AutoMappingList> data, Object property) {
        return data.list().autoMapping(item -> item.property(property));
    }
}
