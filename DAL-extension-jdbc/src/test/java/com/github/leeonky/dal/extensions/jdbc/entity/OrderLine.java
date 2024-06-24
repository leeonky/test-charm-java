package com.github.leeonky.dal.extensions.jdbc.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@Table(name = "order_lines")
public class OrderLine {
    @Id
    private long id;
    @ManyToOne
    private Order order;

    @ManyToOne
    private Product product;
    private int quantity;
    private long refId;
}
