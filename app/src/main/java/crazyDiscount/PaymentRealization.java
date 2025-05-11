package crazyDiscount;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Objects;

public class PaymentRealization {
    private final Integer orderId;
    private final String paymentMethod;
    private BigDecimal loyaltyPoints;

    public PaymentRealization(Integer orderId, String paymentMethod, BigDecimal loyaltyPoints) {
        this.orderId = orderId;
        this.paymentMethod = paymentMethod;
        this.loyaltyPoints = loyaltyPoints;
    }
    public PaymentRealization(Integer orderId, String paymentMethod) {
        this(orderId, paymentMethod, BigDecimal.ZERO);
    }

    public void addLoyalityDiscount(DataBank bank) {
        if (loyaltyPoints.compareTo(BigDecimal.ZERO) == 0) {
        this.loyaltyPoints = loyaltyPoints.add(this.getOrderValue(bank).multiply(bank.LOYALTY_POINTS_DISCOUNT));
        }else {
            throw new RuntimeException("Loyality points already set");
        }
    }

    public boolean isPartialLoyaltyPoints(DataBank bank) {

        return loyaltyPoints.compareTo(BigDecimal.ZERO) > 0 && !bank.LOYALTY_POINTS_DISCOUNT_NAME.equals(paymentMethod);
    }
    public boolean isFullLoyaltyPoints(DataBank bank) {
        return loyaltyPoints.compareTo(BigDecimal.ZERO) > 0 && bank.LOYALTY_POINTS_DISCOUNT_NAME.equals(paymentMethod);
    }

    public BigDecimal getLoyaltyPoints() {
        return loyaltyPoints;
    }
    public Integer getOrderId() {
        return orderId;
    }
    public BigDecimal getOrderValue(DataBank bank) {
        return bank.getOrder(this.orderId).getValue();
    }
    public BigDecimal getOrderValueWithDiscount(DataBank bank) {
        return  this.getOrderValue(bank).subtract(this.getDiscount(bank));
    }
    public String getPaymentMethod() {
        return paymentMethod;
    }

    public BigDecimal getDiscount(DataBank bank) {
        if (this.isPartialLoyaltyPoints(bank)) {
            return bank.LOYALTY_POINTS_DISCOUNT.multiply(this.getOrderValue(bank));
        } else if (bank.getOrder(orderId).getPromotions().contains(this.paymentMethod)) {
            return this.getOrderValue(bank).multiply(bank.getPaymentMethod(paymentMethod).getDiscount());
        }
        else return BigDecimal.ZERO;
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId, paymentMethod, loyaltyPoints);
    }
}
