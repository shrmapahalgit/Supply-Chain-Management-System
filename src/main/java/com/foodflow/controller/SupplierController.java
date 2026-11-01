package com.foodflow.controller;

import com.foodflow.exception.ResourceNotFoundException;
import com.foodflow.model.Supplier;
import com.foodflow.repository.SupplierRepository;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {
    private final SupplierRepository repository;
    public SupplierController(SupplierRepository repository) { this.repository = repository; }
    @GetMapping public List<Supplier> all() { return repository.findAll(); }
    @PostMapping public Supplier create(@Valid @RequestBody Supplier s) { return repository.save(s); }
    @PutMapping("/{id}") public Supplier update(@PathVariable long id, @Valid @RequestBody Supplier s) {
        if (repository.update(id, s) == 0) throw new ResourceNotFoundException("Supplier " + id + " was not found");
        return repository.findById(id).orElseThrow();
    }
    @DeleteMapping("/{id}") public void delete(@PathVariable long id) {
        if (repository.delete(id) == 0) throw new ResourceNotFoundException("Supplier " + id + " was not found");
    }
}
