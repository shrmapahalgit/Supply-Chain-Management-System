package com.foodflow.model;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class FoodProductTest {
    @Test void dairyGetsRefrigerationAdvice() {
        FoodProduct p = new FoodProduct(1,"D-1","Milk","Dairy",3,5,new BigDecimal("55.00"),null,null);
        assertTrue(p.isLowStock());
        assertEquals("Keep refrigerated at 2–5°C", p.getStorageAdvice());
    }
    @Test void quantityCannotBeNegative() {
        FoodProduct p = new FoodProduct();
        assertThrows(IllegalArgumentException.class, () -> p.setQuantity(-1));
    }
}
