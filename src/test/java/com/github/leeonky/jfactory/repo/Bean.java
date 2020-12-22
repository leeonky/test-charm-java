package com.github.leeonky.jfactory.repo;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Getter
@Setter
@Accessors(chain = true)
public class Bean {

    @Id
    public long id;

    public String value;
}
