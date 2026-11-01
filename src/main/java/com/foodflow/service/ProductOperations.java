package com.foodflow.service;

import com.foodflow.model.FoodProduct;
import java.util.List;

public interface ProductOperations {
    List<FoodProduct> getAll();
    FoodProduct get(long id);
    FoodProduct create(FoodProduct product);
    FoodProduct update(long id, FoodProduct product);
    void delete(long id);
}
