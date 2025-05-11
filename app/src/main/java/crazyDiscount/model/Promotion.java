package crazyDiscount.model;

import java.math.BigDecimal;
import java.util.Objects;

public class Promotion {
    private String id;
    private BigDecimal discount;
    private BigDecimal limit;

    // Default constructor
    public Promotion() {
    }

    // Constructor with all fields
    public Promotion(String id, BigDecimal discount, BigDecimal limit) {
        this.id = id;
        this.discount = discount;
        this.limit = limit;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public BigDecimal getLimit() {
        return limit;
    }

    public void setLimit(BigDecimal limit) {
        this.limit = limit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Promotion promotion = (Promotion) o;
        return Objects.equals(id, promotion.id) &&
               Objects.equals(discount, promotion.discount) &&
               Objects.equals(limit, promotion.limit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, discount, limit);
    }

    @Override
    public String toString() {
        return "Promotion{" +
               "id='" + id + '\'' +
               ", discount=" + discount +
               ", limit=" + limit +
               '}';
    }
} 