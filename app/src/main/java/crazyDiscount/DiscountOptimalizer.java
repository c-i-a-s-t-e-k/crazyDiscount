package crazyDiscount;

import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

public interface DiscountOptimalizer {
    public void optimalize();

    public Map<String, BigDecimal> getPaymentMethodOptimalizatedCosts();

    public boolean isOptimalized();
}
