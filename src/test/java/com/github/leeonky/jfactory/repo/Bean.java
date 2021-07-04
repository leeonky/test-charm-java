package com.github.leeonky.jfactory.repo;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Accessors(chain = true)
@Data
public class Bean {

    @Id
    public long id;

    public String value;
}
