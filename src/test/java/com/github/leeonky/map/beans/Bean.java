package com.github.leeonky.map.beans;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Bean {
    private String key, value;
}
