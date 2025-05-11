package crazyDiscount;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.File;
import java.io.IOException;
import com.fasterxml.jackson.databind.JsonNode;

public class PaymentsMethodsBankTest {
    
    @Test
    public void testPaymentMethodsLoadedCorrectly() throws IOException {
        // Get the path to the test resource file
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("paymentmethods.json").getFile());
        String absolutePath = file.getAbsolutePath();
        
        // Create PaymentsMethodsBank with the test file
        PaymentsMethodsBank paymentsMethodsBank = new PaymentsMethodsBank(absolutePath);
        
        // Check if payment methods are loaded
        JsonNode paymentMethods = paymentsMethodsBank.getPaymentMethods();
        assertNotNull("Payment methods should not be null", paymentMethods);
        
        // Check if it's an array
        assertTrue("Payment methods should be an array", paymentMethods.isArray());
        
        // Check if we have the correct number of payment methods
        assertEquals("Should have 3 payment methods", 3, paymentMethods.size());
        
        // Check specific payment method data
        JsonNode firstMethod = paymentMethods.get(0);
        assertEquals("First payment method ID should be PUNKTY", "PUNKTY", firstMethod.get("id").asText());
        assertEquals("First payment method discount should be 15", "15", firstMethod.get("discount").asText());
        assertEquals("First payment method limit should be 100.00", "100.00", firstMethod.get("limit").asText());
        
        // Check a different payment method
        JsonNode secondMethod = paymentMethods.get(1);
        assertEquals("Second payment method ID should be mZysk", "mZysk", secondMethod.get("id").asText());
        assertEquals("Second payment method discount should be 10", "10", secondMethod.get("discount").asText());
        assertEquals("Second payment method limit should be 180.00", "180.00", secondMethod.get("limit").asText());
    }
} 