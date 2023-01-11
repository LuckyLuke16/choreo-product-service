package com.example.productservice.repository;

import com.example.productservice.model.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Item, Integer> {
    List<Item> findByGenre(String genre);
}
