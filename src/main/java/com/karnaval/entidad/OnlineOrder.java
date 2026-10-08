package com.karnaval.entidad;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "online_orders")
public class OnlineOrder {
    @Id
    @Column(length = 36)
    private String id;

    @Version
    private long version;

    @Column(unique = true, length = 255)
    private String stripeSessionId;

    // Nullable so orders created before stock reservation was introduced remain readable.
    private Boolean stockReserved;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private OnlineOrderStatus status;

    @Column(nullable = false)
    private long totalAmount;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant paidAt;

    @Column(length = 255)
    private String customerEmail;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "online_order_lines", joinColumns = @JoinColumn(name = "order_id"))
    @OrderColumn(name = "line_position")
    private List<OnlineOrderLine> lines = new ArrayList<>();

    protected OnlineOrder() {}

    public OnlineOrder(List<OnlineOrderLine> lines) {
        this.id = UUID.randomUUID().toString();
        this.lines = new ArrayList<>(lines);
        this.totalAmount = lines.stream().mapToLong(OnlineOrderLine::getSubtotal).reduce(0L, Math::addExact);
        this.status = OnlineOrderStatus.PENDING;
        this.stockReserved = true;
        this.createdAt = Instant.now();
    }

    public String getId() { return id; }
    public String getStripeSessionId() { return stripeSessionId; }
    public void setStripeSessionId(String stripeSessionId) { this.stripeSessionId = stripeSessionId; }
    public boolean isStockReserved() { return Boolean.TRUE.equals(stockReserved); }
    public void setStockReserved(boolean stockReserved) { this.stockReserved = stockReserved; }
    public OnlineOrderStatus getStatus() { return status; }
    public void setStatus(OnlineOrderStatus status) { this.status = status; }
    public long getTotalAmount() { return totalAmount; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getPaidAt() { return paidAt; }
    public void setPaidAt(Instant paidAt) { this.paidAt = paidAt; }
    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
    public List<OnlineOrderLine> getLines() { return lines; }
}
