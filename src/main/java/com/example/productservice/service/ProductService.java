package com.example.productservice.service;

import com.example.productservice.exception.NoItemsFoundException;
import com.example.productservice.exception.StockNotChangeableException;
import com.example.productservice.exception.StockToLowException;
import com.example.productservice.model.ItemDTO;
import com.example.productservice.model.ItemQuantityDTO;
import com.example.productservice.model.entity.Item;
import com.example.productservice.repository.ProductRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ProductService {

    Logger logger = LoggerFactory.getLogger(ProductService.class);

    private ModelMapper modelMapper;

    private ProductRepository productRepository;

    public ProductService(ProductRepository productRepository, ModelMapper modelMapper) {
        this.productRepository = productRepository;
        this.modelMapper = modelMapper;
    }

    public List<ItemDTO> fetchProductsBySpecificGenre(String genre) {
        List<ItemDTO> allItemDTOs = new ArrayList<>();

        try {
            List<Item> itemsFromDB = productRepository.findByGenre(genre);
            if (!itemsFromDB.isEmpty())
                allItemDTOs = itemToItemDTOList(itemsFromDB);
        } catch (Exception e) {
            throw new NoItemsFoundException();
        }
        return allItemDTOs;
    }

    public List<ItemDTO> fetchAllProducts() {
        List<ItemDTO> allItemDTOs = new ArrayList<>();

        try {
            List<Item> itemsFromDB = productRepository.findAll();
            if (!itemsFromDB.isEmpty())
                allItemDTOs = itemToItemDTOList(itemsFromDB);
        } catch (Exception e) {
            throw new NoItemsFoundException();
        }
        return allItemDTOs;
    }

    private List<ItemDTO> itemToItemDTOList(List<Item> itemsFromDB) {
        List<ItemDTO> itemDTOs = new ArrayList<>();
        for (Item i : itemsFromDB) {
            try {
                itemDTOs.add(modelMapper.map(i, ItemDTO.class));
            } catch (Exception e) {
                logger.warn("Item with id {} could not be converted to an ItemDTO", i.getId());
            }
        }
        return itemDTOs;
    }

    public Item fetchSingleProduct(int itemID) {
        try {
            Optional<Item> singleItemFromDB = productRepository.findById(itemID);
            if (singleItemFromDB.isPresent())
                return singleItemFromDB.get();
            else
                logger.warn("Item with id {} was not found", itemID);
        } catch (Exception e) {
            logger.warn("Item with id {} was not found: ", itemID, e);
        }
        throw new NoItemsFoundException();
    }

    public List<Integer> fetchUnavailableItems(HashMap<Integer, Integer> itemsWithQuantity) {
        List<Integer> idsOfAvailableItemsList;
        List<Integer> idsOfUnchangedItems;

        if (itemsWithQuantity.isEmpty()) {
            throw new NoItemsFoundException();
        }
        idsOfAvailableItemsList = fetchAvailableItems(itemsWithQuantity);
        if(areAllItemsAvailable(itemsWithQuantity, idsOfAvailableItemsList)) {
            idsOfUnchangedItems = this.setQuantityOfItems(idsOfAvailableItemsList, itemsWithQuantity);
            return idsOfUnchangedItems;
        }

        return this.extractAvailableItems(itemsWithQuantity, idsOfAvailableItemsList);
    }

    private List<Integer> extractAvailableItems(HashMap<Integer, Integer> itemsWithQuantity, List<Integer> idsOfAvailableItemsList) {
        for ( Integer i : idsOfAvailableItemsList) {
            itemsWithQuantity.remove(i);
        }

        return itemsWithQuantity.keySet().stream().toList();
    }

    private List<Integer> setQuantityOfItems(List<Integer> idsOfAvailableItemsList, HashMap<Integer, Integer> itemsWithQuantity) {
        List<Integer> idsOfUnchangedItems = new ArrayList<>();

        for (Integer id : idsOfAvailableItemsList) {
            try {
                this.reduceQuantityOfSingleItemInDB(id, itemsWithQuantity.get(id));
            } catch (Exception e) {
                idsOfUnchangedItems.add(id);
                logger.warn("Quantity of item with id: {} could not be changed", id);
            }
        }

        return idsOfUnchangedItems;
    }

    private void reduceQuantityOfSingleItemInDB(Integer id, Integer quantity) {
        Optional<Item> itemOptional = this.productRepository.findById(id);
        if(itemOptional.isEmpty())
            throw new NoItemsFoundException();

        Item itemWithChangedQuantity = itemOptional.get();
        itemWithChangedQuantity.setQuantity(itemWithChangedQuantity.getQuantity() - quantity);
        this.productRepository.deleteById(id);
        this.productRepository.save(itemWithChangedQuantity);
    }

    private boolean areAllItemsAvailable(HashMap<Integer, Integer> itemsWithQuantity, List<Integer> idsOfAvailableItemsList) {
        return idsOfAvailableItemsList.size() == itemsWithQuantity.size();
    }

    private List<Integer> fetchAvailableItems(HashMap<Integer, Integer> itemsWithQuantity) {
        List<Integer> idsOfAvailableItemsList = new ArrayList<>();
        Set<Integer> idsOfItemsToCheckStock = itemsWithQuantity.keySet();

        for (Integer id : idsOfItemsToCheckStock) {
            try {
                this.checkAvailabilityOfSingleItem(id, itemsWithQuantity.get(id));
                idsOfAvailableItemsList.add(id);
            } catch (Exception e) {
                logger.warn("Item with id: {} is not available", id, e);
            }
        }
        return idsOfAvailableItemsList;
    }

    private void checkAvailabilityOfSingleItem(Integer id, Integer quantityToCheck) {

        Optional<Item> itemToCheck = this.productRepository.findById(id);
        if (itemToCheck.isEmpty()) {
            logger.warn("Item with id: {} was not found", id);
            throw new NoItemsFoundException();
        }
        if (itemToCheck.get().getQuantity() < quantityToCheck) {
            logger.warn("Stock of item with id: {} is not high enough", id);
            throw new StockToLowException(id);
        }


    }

    public void addStockOfItem(ItemQuantityDTO itemsWithQuantity) {
        try {
            itemsWithQuantity.getItemsFromShoppingCart().forEach((key, value) -> {
                Optional<Item> itemToChangeOptional = this.productRepository.findById(key);
                if(itemToChangeOptional.isEmpty()) {
                    logger.warn("Item with id: {} not found", key);
                } else {
                    Item itemToChange = itemToChangeOptional.get();
                    int currentQuantity = itemToChange.getQuantity();
                    itemToChangeOptional.get().setQuantity(currentQuantity + value);
                    this.productRepository.deleteById(key);
                    this.productRepository.save(itemToChange);
                }
            });
        } catch (Exception e) {
            throw new StockNotChangeableException();
        }
    }
}
