package crazyDiscount;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.ImmutablePair;


import crazyDiscount.model.Order;
import crazyDiscount.model.Promotion;

public class DataBank {
    private List<Order> orders;
    private Map<String, Promotion> paymentMethods;

    public DataBank(String ordersPath, String paymentMethodsPath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        File ordersFile = new File(ordersPath);
        this.orders = objectMapper.readValue(ordersFile, new TypeReference<List<Order>>(){});

        // Re-use objectMapper or create a new one if needed for different configurations
        File paymentMethodsFile = new File(paymentMethodsPath);
        List<Promotion> tmpPaymentMethods = objectMapper.readValue(paymentMethodsFile, new TypeReference<List<Promotion>>(){});
        this.paymentMethods = new HashMap<>();
        for (Promotion paymentMethod : tmpPaymentMethods) {
            this.paymentMethods.put(paymentMethod.getId(), paymentMethod);
        }
    }

    public Order getOrder(int index) {
        return orders.get(index);
    }

    public Set<String> getPaymentMethodsIds() {
        return paymentMethods.keySet();
    }

    public int getOrdersSize() {
        return orders.size();
    }

    public Promotion getPaymentMethod(String id) {
        return paymentMethods.get(id);
    }

    public Map<String, BigDecimal> getMaxOrdersAmountPerPaymentMethod() {
        Map<String, BigDecimal> result = new HashMap<>();
        for (String paymentMethodId : paymentMethods.keySet()) {
            result.put(paymentMethodId, new BigDecimal(0));
        }
        for (Order order : orders) {
            if ( order.getPromotions() == null || order.getPromotions().isEmpty()) {
                continue;
            }
            for (String paymentMethodId : order.getPromotions()) {
                result.put(paymentMethodId, result.get(paymentMethodId).add(order.getValue()));
            }
        }
        return result;
    }

    public Pair<Promotion, List<Integer>> getMostVaiablePaymentMethod(Set<Integer> unusedOrders, Set<String> unusedPaymentMethodIds, Map<String, BigDecimal> maxOrdersAmountPerPaymentMethod) {
        String selectedPaymentMethod = null;
        BigDecimal maximalWorth = new BigDecimal(0);
        for (String paymentMethodId : unusedPaymentMethodIds) {
            Promotion paymentMethod = this.getPaymentMethod(paymentMethodId);
//            Calculating how much discount we can get for this Payment Methode
            BigDecimal newMaximalWorth = paymentMethod.getLimit().multiply(paymentMethod.getDiscount());
            BigDecimal discountedMaxOrderAmount = maxOrdersAmountPerPaymentMethod.get(paymentMethodId).multiply(paymentMethod.getDiscount());
            if (discountedMaxOrderAmount.compareTo(newMaximalWorth) < 0) {newMaximalWorth = discountedMaxOrderAmount;}

            if (newMaximalWorth.compareTo(maximalWorth) > 0) {
                maximalWorth = newMaximalWorth;
                selectedPaymentMethod = paymentMethodId;
            }
        }
        Set<Integer> selectedOrders = new HashSet<>();
        for (Integer orderId : unusedOrders) {
            if ( this.getOrder(orderId).getPromotions() == null || this.getOrder(orderId).getPromotions().isEmpty()) {
                continue;
            }
            if (this.getOrder(orderId).getPromotions().contains(selectedPaymentMethod)) {
                selectedOrders.add(orderId);
            }
        }
        return new ImmutablePair<>(this.getPaymentMethod(selectedPaymentMethod), new ArrayList<>(selectedOrders));
    }
}
