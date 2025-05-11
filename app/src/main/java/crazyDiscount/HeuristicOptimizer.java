package crazyDiscount;

import java.util.Set;
import com.google.ortools.Loader;
import com.google.ortools.algorithms.KnapsackSolver;
import java.util.ArrayList;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
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

    private Set<Integer> calculatedOrders;
    private Set<String> usedPaymentMethods;
    private Map<String, BigDecimal> paymentUsageAmount;
    private Map<String, BigDecimal> maxOrdersAmountPerPaymentMethod;
    private DataBank dataBank;

    public HeuristicOptimizer(DataBank dataBank) {
        this.calculatedOrders = new HashSet<>();
        this.usedPaymentMethods = new HashSet<>();
        this.paymentUsageAmount = new HashMap<>();
        this.dataBank = dataBank;
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

    @Override
    public void optimalize() {
        int totalOrdersCount = this.dataBank.getOrdersSize();
        Set<String> allPromotionIds = this.dataBank.getPaymentMethodsIds();

        while (this.calculatedOrders.size() < totalOrdersCount && this.usedPaymentMethods.size() < allPromotionIds.size()) {
            Set<Integer> unusedOrderIds = new HashSet<>();
            for (int i = 0; i < totalOrdersCount; i++) {
                unusedOrderIds.add(i);
            }
            unusedOrderIds.removeAll(this.calculatedOrders);

            if (unusedOrderIds.isEmpty()) {
                break;
            }

            Set<String> unusedPromotionIds = new HashSet<>(allPromotionIds);
            unusedPromotionIds.removeAll(this.usedPaymentMethods);

            if (unusedPromotionIds.isEmpty()) {
                break;
            }

            Pair<Promotion, List<Integer>> viablePromotionPair = this.dataBank.getMostVaiablePaymentMethod(
                    unusedOrderIds,
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
                    if (!this.calculatedOrders.contains(orderId)) {
                        this.calculatedOrders.add(orderId);
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
    }

    @Override
    public Map<String, BigDecimal> getPaymentMethodOptimalizatedCosts() {
        return this.paymentUsageAmount;
    }

    
}
