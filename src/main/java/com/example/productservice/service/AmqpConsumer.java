package com.example.productservice.service;

import com.example.productservice.exception.StockNotChangeableException;
import com.example.productservice.model.ItemQuantityDTO;
import com.example.productservice.model.entity.Item;
import com.example.productservice.model.order.ItemDetailDTO;
import com.example.productservice.model.order.OrderDTO;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component
public class AmqpConsumer {

    private final ProductService productService;

    private ModelMapper modelMapper;

    Logger logger = LoggerFactory.getLogger(AmqpConsumer.class);

    @Autowired
    public AmqpConsumer(ProductService productService, ModelMapper modelMapper) {
        this.productService = productService;
        this.modelMapper = modelMapper;
    }

    @RabbitListener(queues = "#{updateStockQueue.name}")
    @SendTo("#{exchange.name}/stock.updated")
    public Message<OrderDTO> processOrder(Message<OrderDTO> orderDetailsMessage) {
        String userId = orderDetailsMessage.getHeaders().get("userId", String.class);
        String correlationId = orderDetailsMessage.getHeaders().get("correlationId", String.class);
        OrderDTO orderDetails = orderDetailsMessage.getPayload();

        try {
            HashMap<Integer, Integer> itemsToOrderWithAmount = orderDetails.getOrderedItems().getItemsFromShoppingCart();
            List<ItemDetailDTO> itemsToPayFor = fetchItemDTOs(itemsToOrderWithAmount);
            orderDetails.setItemsToPay(itemsToPayFor);

            return MessageBuilder
                    .withPayload(orderDetails)
                    .setHeader("userId", userId)
                    .setHeader("correlationId", correlationId)
                    .build();

        } catch(Exception e) {
            logger.warn("Stock of order could not be updated: correlation id: {}, user id: {}", correlationId, userId, e);
            throw new AmqpRejectAndDontRequeueException("updating of stock failed");
        }
    }

    private List<ItemDetailDTO> fetchItemDTOs(HashMap<Integer, Integer> itemsToOrderWithAmount) {
        List<Integer> idsOdUnavailableItems = this.productService.fetchUnavailableItems(itemsToOrderWithAmount);
        List<ItemDetailDTO> itemsToPayFor = new ArrayList<>();

        if(!idsOdUnavailableItems.isEmpty()) {
            throw new StockNotChangeableException();
        }


        for(Integer id: itemsToOrderWithAmount.keySet()) {
            Item fetchedItem = this.productService.fetchSingleProduct(id);
            fetchedItem.setQuantity(itemsToOrderWithAmount.get(id));
            itemsToPayFor.add(modelMapper.map(fetchedItem, ItemDetailDTO.class));
        }

        return itemsToPayFor;
    }

    @RabbitListener(queues = "#{resetStockQueue.name}")
    public void resetStockOfItems(OrderDTO orderDetails, org.springframework.amqp.core.Message message) {
        String correlationId = message.getMessageProperties().getCorrelationId();

        try {
            ItemQuantityDTO orderedItems = orderDetails.getOrderedItems();
            this.productService.addStockOfItem(orderedItems);
            logger.info("Stock of items reset: correlation id: {}", correlationId);
        } catch (Exception e) {
            logger.warn("Stock of order could not be reset: correlation id: {}", correlationId, e);

            throw new AmqpRejectAndDontRequeueException("Item stock reset failed");
        }
    }
}
