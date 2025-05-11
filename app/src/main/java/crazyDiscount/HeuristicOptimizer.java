package crazyDiscount;

import java.awt.desktop.SystemSleepEvent;
import java.util.Set;
import com.google.ortools.Loader;
import com.google.ortools.algorithms.KnapsackSolver;
import java.util.ArrayList;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.tuple.Pair;
import crazyDiscount.model.Order;
import crazyDiscount.model.Promotion;

public class HeuristicOptimizer implements DiscountOptimalizer {
    static {
        try {
            Loader.loadNativeLibraries();
        } catch (UnsatisfiedLinkError e) {
            System.err.println("Native code library failed to load. Please ensure the OR-Tools native libraries are correctly installed and accessible.");
            throw new RuntimeException("Failed to load OR-Tools native libraries", e);
        }
    }

    private final Set<Integer> uncalculatedOrders;
    private final Set<String> usedPaymentMethods;
    private final Map<String, BigDecimal> paymentUsageAmount;
    private final Map<String, BigDecimal> maxOrdersAmountPerPaymentMethod;
    private final DataBank dataBank;

    public HeuristicOptimizer(DataBank dataBank) {
        this.dataBank = dataBank;
        this.uncalculatedOrders = IntStream.rangeClosed(0, dataBank.getOrdersSize()-1)
                .boxed()
                .collect(Collectors.toSet());
        System.out.println(uncalculatedOrders);
        this.usedPaymentMethods = new HashSet<>();
        this.paymentUsageAmount = new HashMap<>();
        for(String paymentId: dataBank.getPaymentMethodsIds()){
            paymentUsageAmount.put(paymentId, BigDecimal.ZERO);
        }
        this.maxOrdersAmountPerPaymentMethod = dataBank.getMaxOrdersAmountPerPaymentMethod();

    }

    private List<Integer> getHighestWorthOrdersForThisPromotion(Promotion paymentMethod, List<Integer> candidateOrderIds) {
        if (candidateOrderIds == null || candidateOrderIds.isEmpty()) {
            return new ArrayList<>();
        }

        int numItems = candidateOrderIds.size();
        long[] values = new long[numItems];
        long[][] weights = new long[1][numItems];

        for (int i = 0; i < numItems; i++) {
            int orderId = candidateOrderIds.get(i);
            Order currentOrder = this.dataBank.getOrder(orderId);
            BigDecimal orderValueDecimal = currentOrder.getValue();

            if (orderValueDecimal == null || orderValueDecimal.signum() < 0) {
                values[i] = 0L;
            } else {
                values[i] = orderValueDecimal.multiply(new BigDecimal("100")).longValueExact();
            }
            weights[0][i] = values[i];
        }

        BigDecimal capacityDecimal = paymentMethod.getLimit();
        long capacity;

        if (capacityDecimal == null || capacityDecimal.signum() < 0) {
            capacity = 0L;
        } else {
            capacity = capacityDecimal.multiply(new BigDecimal("100")).longValueExact();
        }
        final long[] capacities = {capacity};

        KnapsackSolver solver = new KnapsackSolver(
                KnapsackSolver.SolverType.KNAPSACK_MULTIDIMENSION_BRANCH_AND_BOUND_SOLVER,
                "PaymentMethodKnapsackSolver");

        solver.init(values, weights, capacities);
        solver.solve();

        ArrayList<Integer> selectedOrderIds = new ArrayList<>();
        for (int i = 0; i < numItems; i++) {
            if (solver.bestSolutionContains(i)) {
                selectedOrderIds.add(candidateOrderIds.get(i));
            }
        }
        return selectedOrderIds;
    }

    private void spendLoyaltyPoints(){
        // calculating special offert with loyalty points (10% discount for at least 10% loyalty points)
//        BigDecimal loyaltyPoints = dataBank.getPaymentMethod(dataBank.LOYALTY_POINTS_DISCOUNT_NAME).getLimit()
//                .subtract(paymentUsageAmount.get(dataBank.LOYALTY_POINTS_DISCOUNT_NAME));
//        if (loyaltyPoints.compareTo(BigDecimal.ZERO) > 0) {
//            for (Integer orderId : this.uncalculatedOrders) {
//
//            }
//        }
    }

    @Override
    public void optimalize() {
        int ordersSize = this.dataBank.getOrdersSize();
        Set<String> allPromotionIds = this.dataBank.getPaymentMethodsIds();

        // calculating all regural promotion ("PUNKTY" included)
        while (!this.uncalculatedOrders.isEmpty() && this.usedPaymentMethods.size() < allPromotionIds.size()) {
            Set<String> unusedPromotionIds = new HashSet<>(allPromotionIds);
            unusedPromotionIds.removeAll(this.usedPaymentMethods);

            if (unusedPromotionIds.isEmpty()) {
                break;
            }

            Pair<Promotion, List<Integer>> viablePromotionPair = this.dataBank.getMostVaiablePaymentMethod(
                    this.uncalculatedOrders,
                    unusedPromotionIds,
                    this.maxOrdersAmountPerPaymentMethod
            );

            if (viablePromotionPair == null || viablePromotionPair.getLeft() == null) {

                break;
            }

            Promotion currentPromotion = viablePromotionPair.getLeft();
            List<Integer> candidateOrderIdsForPromotion = viablePromotionPair.getRight();
            String currentPromotionId = currentPromotion.getId();

            List<Integer> ordersToProcessForPromotion = getHighestWorthOrdersForThisPromotion(currentPromotion, candidateOrderIdsForPromotion);

            if (!ordersToProcessForPromotion.isEmpty()) {
                this.paymentUsageAmount.putIfAbsent(currentPromotionId, BigDecimal.ZERO);
                BigDecimal promotionDiscountRate = currentPromotion.getDiscount();

                for (Integer orderId : ordersToProcessForPromotion) {
                    if (this.uncalculatedOrders.contains(orderId)) {
                        this.uncalculatedOrders.remove(orderId);
                        Order processedOrder = this.dataBank.getOrder(orderId);
                        BigDecimal orderValue = processedOrder.getValue();

                        BigDecimal discountValue = orderValue.multiply(promotionDiscountRate);
                        this.paymentUsageAmount.put(
                            currentPromotionId,
                            this.paymentUsageAmount.get(currentPromotionId).add(orderValue.subtract(discountValue))
                        );

                        List<String> associatedPaymentMethods = processedOrder.getPromotions();
                        if (associatedPaymentMethods != null) {
                            for (String pmId : associatedPaymentMethods) {
                                if (this.maxOrdersAmountPerPaymentMethod.containsKey(pmId)) {
                                    this.maxOrdersAmountPerPaymentMethod.put(
                                        pmId,
                                        this.maxOrdersAmountPerPaymentMethod.get(pmId).subtract(orderValue)
                                    );
                                }
                            }
                        }
                    }
                }
            }
            this.usedPaymentMethods.add(currentPromotionId);
        }
        this.spendLoyaltyPoints();

    }
    @Override
    public boolean isOptimalized(){
        return this.uncalculatedOrders.isEmpty();
    }

    @Override
    public Map<String, BigDecimal> getPaymentMethodOptimalizatedCosts() {
        return this.paymentUsageAmount;
    }

    
}
