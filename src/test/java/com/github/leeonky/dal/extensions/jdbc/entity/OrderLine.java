package com.github.leeonky.dal.extensions.jdbc.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@Table(name = "order_lines")
public class OrderLine {
    @Id
    private long id;
    private long orderId, productId;
    private int quantity;
}
