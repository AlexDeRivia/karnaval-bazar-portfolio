package com.karnaval.entidad;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class OnlineOrderLine {
    private Long productId;

    @Column(nullable = false, length = 255)
    private String name;

    private int quantity;
    private long unitAmount;

    protected OnlineOrderLine() {}

    public OnlineOrderLine(Long productId, String name, int quantity, long unitAmount) {
        this.productId = productId;
        this.name = name;
        this.quantity = quantity;
        this.unitAmount = unitAmount;
    }

    public Long getProductId() { return productId; }
    public String getName() { return name; }
    public int getQuantity() { return quantity; }
    public long getUnitAmount() { return unitAmount; }
    public long getSubtotal() { return Math.multiplyExact(unitAmount, quantity); }
}
