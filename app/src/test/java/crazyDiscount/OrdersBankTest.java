package crazyDiscount;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.File;
import java.io.IOException;
import com.fasterxml.jackson.databind.JsonNode;

public class OrdersBankTest {
    
    @Test
    public void testOrdersLoadedCorrectly() throws IOException {
        // Get the path to the test resource file
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("orders.json").getFile());
        String absolutePath = file.getAbsolutePath();
        
        // Create OrdersBank with the test file
        OrdersBank ordersBank = new OrdersBank(absolutePath);
        
        // Check if orders are loaded
        JsonNode orders = ordersBank.getOrders();
        assertNotNull("Orders should not be null", orders);
        
        // Check if it's an array
        assertTrue("Orders should be an array", orders.isArray());
        
        // Check if we have the correct number of orders
        assertEquals("Should have 4 orders", 4, orders.size());
        
        // Check specific order data
        JsonNode firstOrder = orders.get(0);
        assertEquals("First order ID should be ORDER1", "ORDER1", firstOrder.get("id").asText());
        assertEquals("First order value should be 100.00", "100.00", firstOrder.get("value").asText());
        
        // Check if promotions array exists and has correct items
        assertTrue("First order should have promotions", firstOrder.has("promotions"));
        JsonNode promotions = firstOrder.get("promotions");
        assertTrue("Promotions should be an array", promotions.isArray());
        assertEquals("Should have 1 promotion", 1, promotions.size());
        assertEquals("First promotion should be mZysk", "mZysk", promotions.get(0).asText());
    }
} 