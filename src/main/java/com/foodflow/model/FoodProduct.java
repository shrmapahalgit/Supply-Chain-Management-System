package com.foodflow.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class FoodProduct extends InventoryItem {
    public FoodProduct() { super(); }

    public FoodProduct(long id, String sku, String name, String category, int quantity,
                       int reorderLevel, BigDecimal unitPrice, LocalDate expiryDate, Long supplierId) {
        super(id, sku, name, category, quantity, reorderLevel, unitPrice, expiryDate, supplierId);
    }

    @Override public String getCategoryLabel() { return getCategory(); }

    // Polymorphism: storage advice changes by product category.
    @Override public String getStorageAdvice() {
        return switch (getCategory().toLowerCase()) {
            case "dairy" -> "Keep refrigerated at 2–5°C";
            case "vegetables", "fruits" -> "Store in a cool, ventilated area";
            case "grains" -> "Keep dry and sealed";
            default -> "Follow supplier storage instructions";
        };
    }
}
