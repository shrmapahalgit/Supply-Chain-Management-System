package com.foodflow.repository;

import com.foodflow.model.FoodProduct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

@Repository
public class ProductRepository {
    private final JdbcTemplate jdbc;
    public ProductRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    private final RowMapper<FoodProduct> mapper = (rs, row) -> new FoodProduct(
        rs.getLong("id"), rs.getString("sku"), rs.getString("name"), rs.getString("category"),
        rs.getInt("quantity"), rs.getInt("reorder_level"), rs.getBigDecimal("unit_price"),
        rs.getDate("expiry_date") == null ? null : rs.getDate("expiry_date").toLocalDate(),
        (Long) rs.getObject("supplier_id")
    );

    public List<FoodProduct> findAll() {
        return jdbc.query("SELECT * FROM products ORDER BY id DESC", mapper);
    }
    public Optional<FoodProduct> findById(long id) {
        return jdbc.query("SELECT * FROM products WHERE id=?", mapper, id).stream().findFirst();
    }
    public FoodProduct save(FoodProduct p) {
        jdbc.update("INSERT INTO products(sku,name,category,quantity,reorder_level,unit_price,expiry_date,supplier_id) VALUES(?,?,?,?,?,?,?,?)",
            p.getSku(), p.getName(), p.getCategory(), p.getQuantity(), p.getReorderLevel(), p.getUnitPrice(),
            p.getExpiryDate() == null ? null : Date.valueOf(p.getExpiryDate()), p.getSupplierId());
        return findById(jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class)).orElseThrow();
    }
    public int update(long id, FoodProduct p) {
        return jdbc.update("UPDATE products SET sku=?,name=?,category=?,quantity=?,reorder_level=?,unit_price=?,expiry_date=?,supplier_id=? WHERE id=?",
            p.getSku(), p.getName(), p.getCategory(), p.getQuantity(), p.getReorderLevel(), p.getUnitPrice(),
            p.getExpiryDate() == null ? null : Date.valueOf(p.getExpiryDate()), p.getSupplierId(), id);
    }
    public int delete(long id) { return jdbc.update("DELETE FROM products WHERE id=?", id); }
    public int count() { return jdbc.queryForObject("SELECT COUNT(*) FROM products", Integer.class); }
    public int lowStockCount() { return jdbc.queryForObject("SELECT COUNT(*) FROM products WHERE quantity <= reorder_level", Integer.class); }
    public List<FoodProduct> lowStock() { return jdbc.query("SELECT * FROM products WHERE quantity <= reorder_level ORDER BY quantity", mapper); }
}
