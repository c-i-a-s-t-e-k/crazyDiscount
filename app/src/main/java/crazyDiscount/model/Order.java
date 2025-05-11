package crazyDiscount.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public class Order {
    private String id;
    private BigDecimal value;
    private List<String> promotions;

    // Default constructor
    public Order() {
    }

    // Constructor with all fields
    public Order(String id, BigDecimal value, List<String> promotions) {
        this.id = id;
        this.value = value;
        this.promotions = promotions;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public List<String> getPromotions() {
        return promotions;
    }

    public void setPromotions(List<String> promotions) {
        this.promotions = promotions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Objects.equals(id, order.id) &&
               Objects.equals(value, order.value) &&
               Objects.equals(promotions, order.promotions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, value, promotions);
    }

    @Override
    public String toString() {
        return "Order{" + "id='" + id + '\''
                 + ", value=" + value +
               ", promotions=" + promotions +
               '}';
    }
} 