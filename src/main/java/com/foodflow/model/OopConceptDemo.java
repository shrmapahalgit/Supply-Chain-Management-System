package com.foodflow.model;

import java.math.BigDecimal;

/**
 * Small classroom demonstration of abstraction, inheritance and runtime polymorphism.
 * Not used to seed database data; useful to discuss during the viva.
 */
public final class OopConceptDemo {
    private OopConceptDemo() {}

    public static String describe(InventoryItem item) {
        // The actual method implementation is chosen at runtime (polymorphism).
        return item.getTrackingLabel() + " | " + item.getCategoryLabel() + " | " + item.getStorageAdvice();
    }

    public static void main(String[] args) {
        InventoryItem item = new FoodProduct(0, "DEMO-01", "Demo Milk", "Dairy",
                4, 10, new BigDecimal("60.00"), null, null);
        System.out.println(describe(item));
        System.out.println("Low stock? " + item.isLowStock());
    }
}
