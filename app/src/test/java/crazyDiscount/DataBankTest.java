package crazyDiscount;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.math.BigDecimal;
import java.net.URL;
import org.apache.commons.lang3.tuple.Pair;
import crazyDiscount.model.Promotion;
import crazyDiscount.DataBank;

public class DataBankTest {

    private DataBank dataBank;
    private final String testOrdersResourceName = "orders.json";
    private final String testPaymentMethodsResourceName = "paymentmethods.json";

    @Before
    public void setUp() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();

        URL ordersResource = classLoader.getResource(testOrdersResourceName);
        if (ordersResource == null) {
            throw new IllegalArgumentException("Test resource file not found: " + testOrdersResourceName);
        }
        File ordersFile = new File(ordersResource.getFile());

        URL paymentMethodsResource = classLoader.getResource(testPaymentMethodsResourceName);
        if (paymentMethodsResource == null) {
            throw new IllegalArgumentException("Test resource file not found: " + testPaymentMethodsResourceName);
        }
        File paymentMethodsFile = new File(paymentMethodsResource.getFile());

        dataBank = new DataBank(ordersFile.getAbsolutePath(), paymentMethodsFile.getAbsolutePath());
    }

    @Test
    public void getOrder() {
        assertNotNull(dataBank.getOrder(0));
        // Add more assertions based on your dummy data
    }
    
    @Test
    public void getPaymentMethods() {
        assertNotNull(dataBank.getPaymentMethodsIds());

        assertTrue(dataBank.getPaymentMethodsIds().contains("PUNKTY"));
        assertTrue(dataBank.getPaymentMethodsIds().contains("mZysk"));
        assertTrue(dataBank.getPaymentMethodsIds().contains("BosBankrut"));
    }

    @Test
    public void getMaxOrdersAmountPerPaymentMethodTest() {
        Set<String> expectedOrders = dataBank.getPaymentMethodsIds();
        Map<String, BigDecimal> maxOrders = dataBank.getMaxOrdersAmountPerPaymentMethod();

        assertEquals(0, maxOrders.get("PUNKTY").compareTo(BigDecimal.ZERO));
        assertEquals(0, maxOrders.get("mZysk").compareTo(new BigDecimal("250")));
        assertEquals(0, maxOrders.get("BosBankrut").compareTo(new BigDecimal("350")));
    }

    @Test
    public void mostValuablePaymentMethod() {
        HashSet<Integer> testUnusedOrders = new HashSet<>(Arrays.asList(0, 1, 2, 3));
        HashSet<String> testUnusedPaymentMethodIds = new HashSet<>(dataBank.getPaymentMethodsIds());

        Map<String, BigDecimal> testMaxOrdersAmountPerPaymentMethod = dataBank.getMaxOrdersAmountPerPaymentMethod();

        assertNotNull("Method call with prepared parameters should not be null",
                dataBank.getMostVaiablePaymentMethod(testUnusedOrders, testUnusedPaymentMethodIds, testMaxOrdersAmountPerPaymentMethod));

        // Second call, using the same, well-defined parameters for detailed assertions.
        Pair<Promotion, List<Integer>> result = dataBank.getMostVaiablePaymentMethod(testUnusedOrders, testUnusedPaymentMethodIds, testMaxOrdersAmountPerPaymentMethod);

        Promotion paymentMethod = result.getLeft();
        List<Integer> actualOrders = result.getRight(); // Renamed from 'orders' to 'actualOrders'

        assertEquals("Payment method ID should be mZysk", "mZysk", paymentMethod.getId());

        assertEquals("Selected orders should be [0, 2]", Arrays.asList(0, 2), actualOrders);
    }

    // Add more test methods for other functionalities of DataBank
} 