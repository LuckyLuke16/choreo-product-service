package com.example.productservice.model.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "items")
public class Item {

    @Id
    private int id;

    private int quantity;

    private String name;

    private String description;

    private String genre;

    private String author;

    private float price;

}
