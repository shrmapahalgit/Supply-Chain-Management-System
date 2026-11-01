package com.foodflow.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Map;

@Repository
public class OrderRepository {
    private final JdbcTemplate jdbc;
    public OrderRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public List<Map<String,Object>> findAll() {
        return jdbc.queryForList("""
            SELECT o.id, o.order_code AS orderCode, s.company_name AS supplier,
                   o.order_date AS orderDate, o.status, o.total_amount AS totalAmount, o.notes
            FROM orders o JOIN suppliers s ON s.id=o.supplier_id ORDER BY o.id DESC
            """);
    }
    public int count() { return jdbc.queryForObject("SELECT COUNT(*) FROM orders", Integer.class); }
    public Map<String,Object> create(long supplierId, long productId, int quantity, String notes) {
        return jdbc.execute((org.springframework.jdbc.core.ConnectionCallback<Map<String,Object>>) con -> {
            con.setAutoCommit(false);
            try {
                java.math.BigDecimal price;
                try (var ps = con.prepareStatement("SELECT unit_price FROM products WHERE id=?")) {
                    ps.setLong(1, productId);
                    try (var rs = ps.executeQuery()) {
                        if (!rs.next()) throw new IllegalArgumentException("Selected product does not exist");
                        price = rs.getBigDecimal(1);
                    }
                }
                String code = "PO-" + java.time.LocalDate.now().getYear() + "-" + java.util.UUID.randomUUID().toString().substring(0,8).toUpperCase();
                long orderId;
                try (var ps = con.prepareStatement("INSERT INTO orders(order_code,supplier_id,status,total_amount,notes) VALUES(?,?,'PENDING',?,?)", java.sql.Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, code); ps.setLong(2, supplierId);
                    ps.setBigDecimal(3, price.multiply(java.math.BigDecimal.valueOf(quantity))); ps.setString(4, notes);
                    ps.executeUpdate();
                    try (var keys = ps.getGeneratedKeys()) { keys.next(); orderId = keys.getLong(1); }
                }
                try (var ps = con.prepareStatement("INSERT INTO order_items(order_id,product_id,quantity,unit_price,line_total) VALUES(?,?,?,?,?)")) {
                    ps.setLong(1, orderId); ps.setLong(2, productId); ps.setInt(3, quantity);
                    ps.setBigDecimal(4, price); ps.setBigDecimal(5, price.multiply(java.math.BigDecimal.valueOf(quantity)));
                    ps.executeUpdate();
                }
                con.commit();
                return java.util.Map.of("id", orderId, "orderCode", code, "totalAmount", price.multiply(java.math.BigDecimal.valueOf(quantity)), "status", "PENDING");
            } catch (java.sql.SQLException | RuntimeException ex) {
                con.rollback();
                throw ex;
            } finally { con.setAutoCommit(true); }
        });
    }
    public int updateStatus(long id, String status) {
        return jdbc.update("UPDATE orders SET status=? WHERE id=?", status, id);
    }
}
