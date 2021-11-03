package com.github.leeonky.jfactory.repo.beans;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

@Entity
@Accessors(chain = true)
@Getter
@Setter
public class CompositeIdBean {

    @EmbeddedId
    private CompositeId id;

    private String value;
}
