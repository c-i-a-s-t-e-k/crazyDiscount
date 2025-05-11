package crazyDiscount;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.math.BigDecimal;
import java.util.Map;

import static org.junit.Assert.*;

public class OptimalizationTest {
    private DataBank dataBank;

    @Before
    public void setUp() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();

        String testOrdersResourceName = "orders.json";
        URL ordersResource = classLoader.getResource(testOrdersResourceName);
        if (ordersResource == null) {
            throw new IllegalArgumentException("Test resource file not found: " + testOrdersResourceName);
        }
        File ordersFile = new File(ordersResource.getFile());

        String testPaymentMethodsResourceName = "paymentmethods.json";
        URL paymentMethodsResource = classLoader.getResource(testPaymentMethodsResourceName);
        if (paymentMethodsResource == null) {
            throw new IllegalArgumentException("Test resource file not found: " + testPaymentMethodsResourceName);
        }
        File paymentMethodsFile = new File(paymentMethodsResource.getFile());

        dataBank = new DataBank(ordersFile.getAbsolutePath(), paymentMethodsFile.getAbsolutePath());
    }

    @Test
    public void testOptimalization() {
        DiscountOptimalizer optimalizer = new  HeuristicOptimizer(dataBank);

        optimalizer.optimalize();

        Map<String, BigDecimal> paymentMethodOptimalizatedCosts = optimalizer.getPaymentMethodOptimalizatedCosts();

        assertNotNull("Payment method optimized costs should not be null", paymentMethodOptimalizatedCosts);

        for (Map.Entry<String, BigDecimal> entry : paymentMethodOptimalizatedCosts.entrySet()) {
            String paymentMethodId = entry.getKey();
            BigDecimal cost = entry.getValue();

            crazyDiscount.model.Promotion promotion = dataBank.getPaymentMethod(paymentMethodId);
            assertNotNull("Promotion should not be null for ID: " + paymentMethodId, promotion);

            BigDecimal limit = promotion.getLimit();
            assertNotNull("Promotion limit should not be null for ID: " + paymentMethodId, limit);

            assertTrue("Cost for " + paymentMethodId + " should be non-negative. Was: " + cost,
                    cost.compareTo(BigDecimal.ZERO) >= 0);
            assertTrue("Cost for " + paymentMethodId + " (" + cost + ") should not exceed its limit (" + limit + ")",
                    cost.compareTo(limit) <= 0);
        }
    }

    @Test
    public void testOptimalizationProcessTest() {
        DiscountOptimalizer optimalizer = new  HeuristicOptimizer(dataBank);
        assertFalse(optimalizer.isOptimalized());
        optimalizer.optimalize();
        assertTrue(optimalizer.isOptimalized());
    }

    @Test
    public void testBruteForceOptimalization() {
        DiscountOptimalizer optimalizer = new BruteForceOptimizer(dataBank);
        assertFalse(optimalizer.isOptimalized());
        optimalizer.optimalize();
        assertTrue(optimalizer.isOptimalized());
    }
}
