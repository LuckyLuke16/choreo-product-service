package com.example.productservice;

import com.example.productservice.model.ItemQuantityDTO;
import com.example.productservice.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import config.TestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContext;

import java.util.HashMap;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TestConfig.class})
@WebAppConfiguration
public class ProductServiceIT extends DatabaseInitializer {



    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private WebApplicationContext webApplicationContext;


    @BeforeEach
    void setup() {
        this.productRepository.deleteAll();
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
        fillDatabaseWithTestData();
    }

    @Test
    public void Should_Get_Product_Beans() {
        ServletContext servletContext = webApplicationContext.getServletContext();

        assertNotNull(servletContext);
        assertTrue(servletContext instanceof MockServletContext);
        webApplicationContext.getServletContext();
        assertNotNull(webApplicationContext.getBean("productController"));
        assertNotNull(webApplicationContext.getBean("productService"));
    }

    @Test
    public void Should_Fetch_ALL_Items() throws Exception {
        this.mockMvc.perform(get("/items?genre=ALL"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$.[0].id").value(1))
                .andExpect(jsonPath("$.[1].id").value(2));
    }

    @Test
    public void Should_Fetch_Single_Item() throws Exception {
        this.mockMvc.perform(get("/items/1"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void Should_Check_Inventory() throws Exception {
        HashMap<Integer, Integer> itemsWithQuantity = new HashMap<>();
        itemsWithQuantity.put(1, 2);
        itemsWithQuantity.put(2, 1);
        ItemQuantityDTO payload = new ItemQuantityDTO(itemsWithQuantity);
        int expectedQuantity = this.productRepository.findById(2).get().getQuantity() - 1;

        this.mockMvc.perform(MockMvcRequestBuilders.post("/items/stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(payload)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        int updatedQuantity = this.productRepository.findById(2).get().getQuantity();
        assertEquals(expectedQuantity, updatedQuantity);
    }

    @Test
    public void Should_Return_Id_2345() throws Exception {
        HashMap<Integer, Integer> itemsWithQuantity = new HashMap<>();
        itemsWithQuantity.put(2345, 2);
        itemsWithQuantity.put(2, 1);
        ItemQuantityDTO payload = new ItemQuantityDTO(itemsWithQuantity);

        this.mockMvc.perform(MockMvcRequestBuilders.post("/items/stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(payload)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$.[0]").value(2345));
    }

    @Test
    public void Should_Return_Id_2() throws Exception {
        HashMap<Integer, Integer> itemsWithQuantity = new HashMap<>();
        itemsWithQuantity.put(2, 10);
        ItemQuantityDTO payload = new ItemQuantityDTO(itemsWithQuantity);

        this.mockMvc.perform(MockMvcRequestBuilders.post("/items/stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(payload)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$.[0]").value(2));
    }

    @Test
    public void Should_Reset_Stock() throws Exception {
        HashMap<Integer, Integer> itemsWithQuantity = new HashMap<>();
        itemsWithQuantity.put(2345, 2);
        itemsWithQuantity.put(2, 1);
        ItemQuantityDTO payload = new ItemQuantityDTO(itemsWithQuantity);
        int expectedQuantity = this.productRepository.findById(2).get().getQuantity() + 1;

        this.mockMvc.perform(MockMvcRequestBuilders.post("/items/stock/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(payload)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(status().isOk());

        int updatedQuantity = this.productRepository.findById(2).get().getQuantity();
        assertEquals(expectedQuantity, updatedQuantity);
    }

}
