package com.foodflow.controller;

import com.foodflow.exception.ResourceNotFoundException;
import com.foodflow.model.OrderRequest;
import com.foodflow.model.StatusRequest;
import com.foodflow.repository.OrderRepository;
import com.foodflow.repository.SupplierRepository;
import jakarta.validation.Valid;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderRepository orders;
    private final SupplierRepository suppliers;
    private final JdbcTemplate jdbc;
    private static final Set<String> ALLOWED = Set.of("PENDING","APPROVED","IN_TRANSIT","DELIVERED","CANCELLED");

    public OrderController(OrderRepository orders, SupplierRepository suppliers, JdbcTemplate jdbc) {
        this.orders = orders; this.suppliers = suppliers; this.jdbc = jdbc;
    }
    @GetMapping public List<Map<String,Object>> all() { return orders.findAll(); }
    @PostMapping public Map<String,Object> create(@Valid @RequestBody OrderRequest request) {
        if (suppliers.findById(request.getSupplierId()).isEmpty()) throw new ResourceNotFoundException("Supplier was not found");
        return orders.create(request.getSupplierId(), request.getProductId(), request.getQuantity(), request.getNotes());
    }
    @PatchMapping("/{id}/status") public Map<String,Object> status(@PathVariable long id, @Valid @RequestBody StatusRequest request) {
        String status = request.getStatus().trim().toUpperCase();
        if (!ALLOWED.contains(status)) throw new IllegalArgumentException("Status must be one of " + ALLOWED);
        if (orders.updateStatus(id, status) == 0) throw new ResourceNotFoundException("Order " + id + " was not found");
        return Map.of("message", "Order status updated", "status", status);
    }
}
