package com.example.productservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemQuantityDTO {

    private HashMap<Integer, Integer> itemsFromShoppingCart;
}
