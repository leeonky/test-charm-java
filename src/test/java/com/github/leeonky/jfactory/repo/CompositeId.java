package com.github.leeonky.jfactory.repo;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@Accessors(chain = true)
@Data
public class CompositeId implements Serializable {
    private String key1, key2;
}
