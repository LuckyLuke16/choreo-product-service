package com.example.productservice.model.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemDetailDTO {

    private int id;

    private String name;

    private String description;

    private String genre;

    private String author;

    private float price;

    private int quantity;
}
