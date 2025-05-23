package com.github.leeonky.dal.runtime;

import java.util.function.Function;

public class AutoMappingList extends DALCollection.Decorated<Object> {
    public AutoMappingList(Function<Data<?>, Data<?>> mapper, DALCollection<Data<?>> list) {
        super(list.map((index, data) -> {
            try {
                return mapper.apply(data).value();
            } catch (Exception e) {
                throw new ListMappingElementAccessException(index, e);
            }
        }));
    }
}
