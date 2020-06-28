package edu.qut.inventory.service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.qut.inventory.service.repository.InventoryItemRepository;
import edu.qut.model.InventoryItem;
import edu.qut.model.Manufacturer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import javax.annotation.Resource;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:test-application.properties")
public class InventoryControllerTest {
    @Resource
    private MockMvc mockMvc;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private InventoryItemRepository itemRepository;

    @Before
    public void setUp() {
        itemRepository.deleteAll();
    }

    @Test
    public void paginateInventoryList() throws Exception {
        for (int i = 0; i < 10; i++) {
            performCreateInventoryRestCall(createInventoryItem());
        }

        MvcResult result = mockMvc.perform(get("/inventory")
                .param("skip", "0").param("limit", "10"))
                .andDo(print()).andExpect(status().isOk()).andReturn();
        InventoryItem[] items = objectMapper.readValue(result.getResponse().getContentAsString(), InventoryItem[].class);
        assertEquals(10, items.length);

        result = mockMvc.perform(get("/inventory")
                .param("skip", "10").param("limit", "50"))
                .andDo(print()).andExpect(status().isOk()).andReturn();

        String content = result.getResponse().getContentAsString();
        assertEquals(0, content.length());

        result = mockMvc.perform(get("/inventory")
                .param("skip", "5").param("limit", "10"))
                .andDo(print()).andExpect(status().isOk()).andReturn();
        items = objectMapper.readValue(result.getResponse().getContentAsString(), InventoryItem[].class);
        assertEquals(5, items.length);

        result = mockMvc.perform(get("/inventory")
                .param("skip", "8").param("limit", "2"))
                .andDo(print()).andExpect(status().isOk()).andReturn();
        items = objectMapper.readValue(result.getResponse().getContentAsString(), InventoryItem[].class);
        assertEquals(2, items.length);
    }

    @Test
    public void shouldNotFindInventoryItem() throws Exception {
        UUID inventoryId = UUID.randomUUID();
        MvcResult result = mockMvc.perform(get("/inventory/" + inventoryId.toString()))
                .andDo(print()).andExpect(status().isNotFound()).andReturn();
        assertEquals(0, result.getResponse().getContentLength());
    }

    @Test
    public void shouldNotReturnAnyResults() throws Exception {
        MvcResult result = mockMvc.perform(get("/inventory?skip=0&limit=50"))
                .andDo(print()).andExpect(status().isOk()).andReturn();
        assertEquals(0, result.getResponse().getContentLength());
    }

    @Test
    public void invalidInventoryQueryValues() throws Exception {
        mockMvc.perform(get("/inventory?skip=-1&limit=50"))
                .andDo(print()).andExpect(status().isBadRequest());
        mockMvc.perform(get("/inventory?skip=0&limit=51"))
                .andDo(print()).andExpect(status().isBadRequest());
        mockMvc.perform(get("/inventory?skip=-1&limit=51"))
                .andDo(print()).andExpect(status().isBadRequest());
    }

    @Test
    public void createNewInventoryItem() throws Exception {
        InventoryItem inventoryItem = createInventoryItem();
        MvcResult result = performCreateInventoryRestCall(inventoryItem);
        assertEquals(0, result.getResponse().getContentLength());

        result = mockMvc.perform(get("/inventory/" + inventoryItem.getId().toString())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();
        InventoryItem searchResult = objectMapper.readValue(result.getResponse().getContentAsString(), InventoryItem.class);
        assertNotNull(searchResult);

        assertEquals(inventoryItem.getId(), searchResult.getId());
        assertEquals(inventoryItem.getName(), searchResult.getName());
        assertEquals(inventoryItem.getReleaseDate().toInstant().toEpochMilli(),
                searchResult.getReleaseDate().toInstant().toEpochMilli());
        Manufacturer manufacturer = inventoryItem.getManufacturer();
        assertEquals(manufacturer.getHomePage(), manufacturer.getHomePage());
        assertEquals(manufacturer.getName(), manufacturer.getName());
        assertEquals(manufacturer.getPhone(), manufacturer.getPhone());
    }

    @Test
    public void createDuplicateInventoryItem() throws Exception {
        InventoryItem inventoryItem = createInventoryItem();
        MvcResult result = performCreateInventoryRestCall(inventoryItem);
        assertEquals(0, result.getResponse().getContentLength());

        result = mockMvc.perform(post("/inventory")
                .content(objectMapper.writeValueAsString(inventoryItem))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict()).andReturn();
        assertEquals(0, result.getResponse().getContentLength());
    }


    @Test
    public void createInvalidInventoryItem() throws Exception {
        InventoryItem inventoryItem = createInventoryItem();
        inventoryItem.setId(null);
        MvcResult result = mockMvc.perform(post("/inventory")
                .content(objectMapper.writeValueAsString(inventoryItem))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest()).andReturn();
        assertEquals(0, result.getResponse().getContentLength());

        inventoryItem = createInventoryItem();
        inventoryItem.setName(null);
        result = mockMvc.perform(post("/inventory")
                .content(objectMapper.writeValueAsString(inventoryItem))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest()).andReturn();
        assertEquals(0, result.getResponse().getContentLength());

        inventoryItem = createInventoryItem();
        inventoryItem.setReleaseDate(null);
        result = mockMvc.perform(post("/inventory")
                .content(objectMapper.writeValueAsString(inventoryItem))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest()).andReturn();
        assertEquals(0, result.getResponse().getContentLength());

        inventoryItem = createInventoryItem();
        inventoryItem.setManufacturer(null);
        result = mockMvc.perform(post("/inventory")
                .content(objectMapper.writeValueAsString(inventoryItem))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest()).andReturn();
        assertEquals(0, result.getResponse().getContentLength());

        inventoryItem = createInventoryItem();
        Manufacturer manufacturer = inventoryItem.getManufacturer();
        manufacturer.setHomePage(null);
        result = mockMvc.perform(post("/inventory")
                .content(objectMapper.writeValueAsString(inventoryItem))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated()).andReturn();
        assertEquals(0, result.getResponse().getContentLength());

        inventoryItem = createInventoryItem();
        manufacturer = inventoryItem.getManufacturer();
        manufacturer.setName(null);
        result = mockMvc.perform(post("/inventory")
                .content(objectMapper.writeValueAsString(inventoryItem))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest()).andReturn();
        assertEquals(0, result.getResponse().getContentLength());

        inventoryItem = createInventoryItem();
        manufacturer = inventoryItem.getManufacturer();
        manufacturer.setPhone(null);
        result = mockMvc.perform(post("/inventory")
                .content(objectMapper.writeValueAsString(inventoryItem))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated()).andReturn();
        assertEquals(0, result.getResponse().getContentLength());
    }


    private InventoryItem createInventoryItem() {
        InventoryItem inventoryItem = new InventoryItem();
        UUID inventoryId = UUID.randomUUID();
        inventoryItem.setId(inventoryId);
        inventoryItem.setReleaseDate(OffsetDateTime.now());
        inventoryItem.setName("Item 1");
        Manufacturer manufacturer = new Manufacturer();
        inventoryItem.setManufacturer(manufacturer);
        manufacturer.setPhone("1800");
        manufacturer.setName("Acme Pty Ltd");
        manufacturer.setHomePage("http://acme.com");

        return inventoryItem;
    }

    private MvcResult performCreateInventoryRestCall(InventoryItem inventoryItem) throws Exception {
        return mockMvc.perform(post("/inventory")
                .content(objectMapper.writeValueAsString(inventoryItem))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated()).andReturn();
    }
}
