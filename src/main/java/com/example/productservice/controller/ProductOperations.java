package com.example.productservice.controller;

import com.example.productservice.model.ItemDTO;
import com.example.productservice.model.ItemQuantityDTO;
import com.example.productservice.model.entity.Item;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/items")
public interface ProductOperations {

    @GetMapping
    List<ItemDTO> fetchItemsByGenre(@RequestParam String genre);

    @GetMapping("/{itemID}")
    Item fetchSingleItem(@PathVariable int itemID);

    @PostMapping("/stock")
    ResponseEntity<List<Integer>> checkStockOfItems(@RequestBody ItemQuantityDTO itemsWithQuantity);

    @PostMapping("/stock/reset")
    ResponseEntity<String> resetStockOfItems(@RequestBody ItemQuantityDTO itemsWithQuantity);

}
