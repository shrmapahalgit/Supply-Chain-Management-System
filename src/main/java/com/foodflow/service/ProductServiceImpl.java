package com.foodflow.service;

import com.foodflow.exception.ResourceNotFoundException;
import com.foodflow.model.FoodProduct;
import com.foodflow.repository.ProductRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductOperations {
    private final ProductRepository repository;
    public ProductServiceImpl(ProductRepository repository) { this.repository = repository; }

    @Override public List<FoodProduct> getAll() { return repository.findAll(); }
    @Override public FoodProduct get(long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product " + id + " was not found"));
    }
    @Override public FoodProduct create(FoodProduct product) { return repository.save(product); }
    @Override public FoodProduct update(long id, FoodProduct product) {
        if (repository.update(id, product) == 0) throw new ResourceNotFoundException("Product " + id + " was not found");
        return get(id);
    }
    @Override public void delete(long id) {
        if (repository.delete(id) == 0) throw new ResourceNotFoundException("Product " + id + " was not found");
    }
}
