package crazyDiscount;

import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BruteForceOptimizer implements DiscountOptimalizer{
    private final DataBank dataBank;
    private final Map<String, BigDecimal> paymentsCosts = new HashMap<>();
    private final Set<Integer> compudedOrders = new HashSet<>();

    BruteForceOptimizer(DataBank dataBank){
        this.dataBank = dataBank;
        for(String paymentId: dataBank.getPaymentMethodsIds()){
            paymentsCosts.put(paymentId, BigDecimal.ZERO);
        }
    }
    private BigDecimal getDiscountSum(List<PaymentRealization> paymentRealizations){
        BigDecimal sum = BigDecimal.ZERO;
        for(PaymentRealization paymentRealization: paymentRealizations){
            sum = sum.add(paymentRealization.getDiscount(dataBank));
        }
        return sum;
    }

    private void updatePaymentsState(HashMap<String, BigDecimal> paymentsState, PaymentRealization bestPayment){
        if(Objects.equals(bestPayment, null)){
            throw new RuntimeException("There is no payment that can be found");
        }
        if(bestPayment.isPartialLoyaltyPoints(dataBank)){
            paymentsState.put(dataBank.LOYALTY_POINTS_DISCOUNT_NAME,
                    paymentsState.get(dataBank.LOYALTY_POINTS_DISCOUNT_NAME).subtract(bestPayment.getLoyaltyPoints()));
            paymentsState.put(bestPayment.getPaymentMethod(),
                    paymentsState.get(
                            bestPayment.getPaymentMethod()).subtract(
                                    bestPayment.getOrderValue(dataBank)
                    ).add(bestPayment.getLoyaltyPoints()));

        }else{
            paymentsState.put(bestPayment.getPaymentMethod(),
                    bestPayment.getOrderValueWithDiscount(dataBank));
        }

    }

    private Pair<List<PaymentRealization>, HashMap<String, BigDecimal>> calculateBestPaymentForLastOrder(
            int orderIndex, HashMap<String, BigDecimal> paymentsState) {
        return Pair.of(new ArrayList<>(), paymentsState);
    }

    private Pair<List<PaymentRealization>, HashMap<String, BigDecimal>> getPaymentsCosts(
            int n, int i, HashMap<String, BigDecimal> paymentsState)
    {
        if(i == n){
            return calculateBestPaymentForLastOrder(i, paymentsState);
        }

        PaymentRealization bestPayment = null;
        PaymentRealization currentPayment = null;
        BigDecimal currentDiscount = null;
        BigDecimal bestDiscount = null;
        HashMap<String, BigDecimal> currentState = null;
        HashMap<String, BigDecimal> bestState = null;
        List<PaymentRealization> bestRealizations = null;
        BigDecimal orderValue = dataBank.getOrder(i).getValue();

        for (String paymentId : dataBank.getPaymentMethodsIds()){
            BigDecimal paymentLimit = dataBank.getPaymentMethod(paymentId).getLimit();
            BigDecimal paymentDiscount = dataBank.getPaymentMethod(paymentId).getDiscount();

            //buy whole with "PUNKTY"
            if(paymentId.equals(this.dataBank.LOYALTY_POINTS_DISCOUNT_NAME)){
                currentPayment = new PaymentRealization(i, paymentId, new PaymentRealization(i, paymentId).getOrderValueWithDiscount(dataBank));
                if (currentPayment.getLoyaltyPoints().compareTo(paymentsState.get(paymentId)) <= 0 ){
                    currentState = new HashMap<>(paymentsState);
                    updatePaymentsState(currentState, currentPayment);
                    currentDiscount = currentPayment.getDiscount(dataBank);
                    Pair<List<PaymentRealization>, HashMap<String, BigDecimal>> tmp = getPaymentsCosts(n, i+1, currentState);
                    currentDiscount = currentDiscount.add(this.getDiscountSum(tmp.getLeft()));
                    if (bestDiscount == null || currentDiscount.compareTo(bestDiscount) >= 0) {
                        bestPayment = currentPayment;
                        bestDiscount = currentDiscount;
                        bestState = tmp.getRight();
                        bestRealizations = tmp.getLeft();
                    }
                }
            }else {
                //buy with payment methode only
                currentPayment = new PaymentRealization(i, paymentId);
                currentDiscount = currentPayment.getDiscount(dataBank);
                currentState = new HashMap<>(paymentsState);
                updatePaymentsState(currentState, currentPayment);
                Pair<List<PaymentRealization>, HashMap<String, BigDecimal>> tmp = getPaymentsCosts(n, i, currentState);
                currentDiscount = currentDiscount.add(this.getDiscountSum(tmp.getLeft()));
                if (bestDiscount.equals(null) || currentDiscount.compareTo(bestDiscount) >= 0) {
                    bestPayment = currentPayment;
                    bestDiscount = currentDiscount;
                    bestState = tmp.getRight();
                    bestRealizations = tmp.getLeft();
                }
            }
            //special loyality discount
            currentPayment = new PaymentRealization(i, paymentId);
            currentPayment.addLoyalityDiscount(dataBank);
            if(paymentsState.get(dataBank.LOYALTY_POINTS_DISCOUNT_NAME).compareTo(currentPayment.getLoyaltyPoints()) >= 0){
                currentDiscount = currentPayment.getDiscount(dataBank);
                currentState = new HashMap<>(paymentsState);
                updatePaymentsState(currentState, currentPayment);
                Pair<List<PaymentRealization>, HashMap<String, BigDecimal>> tmp = getPaymentsCosts(n, i, currentState);
                currentDiscount = currentDiscount.add(this.getDiscountSum(tmp.getLeft()));
                if (bestDiscount.equals(null) || currentDiscount.compareTo(bestDiscount) >= 0) {
                    bestPayment = currentPayment;
                    bestDiscount = currentDiscount;
                    bestState = tmp.getRight();
                    bestRealizations = tmp.getLeft();
                }
            }
        }
        updatePaymentsState(bestState, bestPayment);
        bestRealizations.add(bestPayment);
        return Pair.of(bestRealizations, bestState);

    }

    @Override
    public void optimalize() {
        int n = dataBank.getOrdersSize();
        HashMap<String, BigDecimal> paymentsState = new HashMap<>();
        for(String paymentId : dataBank.getPaymentMethodsIds()){
            paymentsState.put(paymentId, dataBank.getPaymentMethod(paymentId).getLimit());
        }
        Pair<List<PaymentRealization>, HashMap<String, BigDecimal>> tmp = getPaymentsCosts(n, 0, paymentsState);
        for (PaymentRealization paymentRealization : tmp.getLeft()) {
            String paymentMethode = paymentRealization.getPaymentMethod();
            paymentsCosts.put(paymentMethode, paymentsCosts.get(paymentMethode).add(paymentRealization.getOrderValueWithDiscount(dataBank)));
            this.compudedOrders.add(paymentRealization.getOrderId());
        }

    }

    @Override
    public Map<String, BigDecimal> getPaymentMethodOptimalizatedCosts() {
        return this.paymentsCosts;
    }

    @Override
    public boolean isOptimalized() {
        return this.compudedOrders.size() == dataBank.getOrdersSize();
    }
}
