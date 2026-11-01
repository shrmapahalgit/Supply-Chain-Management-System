package com.foodflow.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Abstract base class for stock-bearing items.
 * Encapsulation is demonstrated by private state and controlled accessors.
 */
public abstract class InventoryItem implements Trackable {
    private long id;
    private String sku;
    private String name;
    private String category;
    private int quantity;
    private int reorderLevel;
    private BigDecimal unitPrice;
    private LocalDate expiryDate;
    private Long supplierId;

    protected InventoryItem() {}

    protected InventoryItem(long id, String sku, String name, String category, int quantity,
                           int reorderLevel, BigDecimal unitPrice, LocalDate expiryDate, Long supplierId) {
        this.id = id;
        setSku(sku); setName(name); setCategory(category);
        setQuantity(quantity); setReorderLevel(reorderLevel); setUnitPrice(unitPrice);
        this.expiryDate = expiryDate;
        this.supplierId = supplierId;
    }

    public abstract String getCategoryLabel();
    public abstract String getStorageAdvice();

    @Override public String getTrackingLabel() { return sku + " — " + name; }
    @Override public boolean isLowStock() { return quantity <= reorderLevel; }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getSku() { return sku; }
    public void setSku(String sku) {
        if (sku == null || sku.isBlank()) throw new IllegalArgumentException("SKU is required");
        this.sku = sku.trim();
    }
    public String getName() { return name; }
    public void setName(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Product name is required");
        this.name = name.trim();
    }
    public String getCategory() { return category; }
    public void setCategory(String category) {
        if (category == null || category.isBlank()) throw new IllegalArgumentException("Category is required");
        this.category = category.trim();
    }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) {
        if (quantity < 0) throw new IllegalArgumentException("Quantity cannot be negative");
        this.quantity = quantity;
    }
    public int getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(int reorderLevel) {
        if (reorderLevel < 0) throw new IllegalArgumentException("Reorder level cannot be negative");
        this.reorderLevel = reorderLevel;
    }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) {
        if (unitPrice == null || unitPrice.signum() < 0) throw new IllegalArgumentException("Price must be zero or more");
        this.unitPrice = unitPrice;
    }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }
}
