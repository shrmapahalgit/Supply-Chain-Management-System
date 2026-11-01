package com.foodflow.controller;

import com.foodflow.repository.OrderRepository;
import com.foodflow.repository.ProductRepository;
import com.foodflow.repository.SupplierRepository;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class DashboardController {
    private final ProductRepository products;
    private final SupplierRepository suppliers;
    private final OrderRepository orders;
    public DashboardController(ProductRepository products, SupplierRepository suppliers, OrderRepository orders) {
        this.products = products; this.suppliers = suppliers; this.orders = orders;
    }
    @GetMapping("/dashboard")
    public Map<String,Object> summary() {
        return Map.of("products", products.count(), "suppliers", suppliers.count(),
                      "orders", orders.count(), "lowStock", products.lowStockCount());
    }
}
