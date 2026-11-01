package com.foodflow.controller;

import com.foodflow.model.FoodProduct;
import com.foodflow.repository.ProductRepository;
import com.foodflow.service.ProductOperations;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductOperations service;
    private final ProductRepository repository;
    public ProductController(ProductOperations service, ProductRepository repository) {
        this.service = service; this.repository = repository;
    }
    @GetMapping public List<FoodProduct> all() { return service.getAll(); }
    @GetMapping("/{id}") public FoodProduct one(@PathVariable long id) { return service.get(id); }
    @GetMapping("/low-stock") public List<FoodProduct> lowStock() { return repository.lowStock(); }
    @PostMapping public FoodProduct create(@Valid @RequestBody FoodProduct p) { return service.create(p); }
    @PutMapping("/{id}") public FoodProduct update(@PathVariable long id, @Valid @RequestBody FoodProduct p) { return service.update(id, p); }
    @DeleteMapping("/{id}") public void delete(@PathVariable long id) { service.delete(id); }
}
