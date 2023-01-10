package com.example.productservice.model.order;

import com.example.productservice.model.ItemDTO;
import com.example.productservice.model.ItemQuantityDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDTO {

    private String paymentMethod;

    private Address address;

    private ItemQuantityDTO orderedItems;

    private List<ItemDetailDTO> itemsToPay;
}
