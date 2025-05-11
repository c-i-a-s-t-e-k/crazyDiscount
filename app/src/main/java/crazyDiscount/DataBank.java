package crazyDiscount;

import java.util.*;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.ImmutablePair;


import crazyDiscount.model.Order;
import crazyDiscount.model.Promotion;

public class DataBank {
    public final String LOYALTY_POINTS_DISCOUNT_NAME = "PUNKTY";
    public final BigDecimal LOYALTY_POINTS_DISCOUNT = new BigDecimal("0.10");
    private final List<Order> orders;
    private final Map<String, Promotion> paymentMethods;

    public DataBank(String ordersPath, String paymentMethodsPath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        File ordersFile = new File(ordersPath);
        this.orders = objectMapper.readValue(ordersFile, new TypeReference<List<Order>>(){});
        for (Order order : this.orders) {
            if (order.getPromotions() == null) {
                order.setPromotions(new ArrayList<>(Collections.singleton(LOYALTY_POINTS_DISCOUNT_NAME)));
            }else if (! order.getPromotions().contains(LOYALTY_POINTS_DISCOUNT_NAME)) {
                order.getPromotions().add(LOYALTY_POINTS_DISCOUNT_NAME);
            }
        }

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

    public Set<Integer> getUnusedOrderIds(Set<Integer> usedOrderIds) {
        Set<Integer> allOrderIds = IntStream.rangeClosed(0, getOrdersSize()-1)
                .boxed()
                .collect(Collectors.toSet());

        allOrderIds.removeAll(usedOrderIds);
        return allOrderIds;
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
