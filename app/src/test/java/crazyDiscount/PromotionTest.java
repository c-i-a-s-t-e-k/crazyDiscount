package crazyDiscount;

import crazyDiscount.model.Promotion;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.Stack;

import static org.junit.Assert.assertTrue;

public class PromotionTest {

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
    public void testOrderDiscount() {
        for(String paymentMethode : dataBank.getPaymentMethodsIds()) {
            Promotion promotion = dataBank.getPaymentMethod(paymentMethode);
            assertTrue(promotion.getDiscount().compareTo(BigDecimal.ONE) < 0);
        }
    }
}
