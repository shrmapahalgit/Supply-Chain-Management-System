package com.foodflow.repository;

import com.foodflow.model.Supplier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class SupplierRepository {
    private final JdbcTemplate jdbc;
    public SupplierRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }
    private final RowMapper<Supplier> mapper = (rs, row) -> new Supplier(
        rs.getLong("id"), rs.getString("company_name"), rs.getString("contact_name"),
        rs.getString("email"), rs.getString("phone"), rs.getString("city"), rs.getString("status"));

    public List<Supplier> findAll() { return jdbc.query("SELECT * FROM suppliers ORDER BY id DESC", mapper); }
    public Optional<Supplier> findById(long id) {
        return jdbc.query("SELECT * FROM suppliers WHERE id=?", mapper, id).stream().findFirst();
    }
    public Supplier save(Supplier s) {
        jdbc.update("INSERT INTO suppliers(company_name,contact_name,email,phone,city,status) VALUES(?,?,?,?,?,?)",
            s.getCompanyName(), s.getContactName(), s.getEmail(), s.getPhone(), s.getCity(), s.getStatus());
        return findById(jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class)).orElseThrow();
    }
    public int update(long id, Supplier s) {
        return jdbc.update("UPDATE suppliers SET company_name=?,contact_name=?,email=?,phone=?,city=?,status=? WHERE id=?",
            s.getCompanyName(), s.getContactName(), s.getEmail(), s.getPhone(), s.getCity(), s.getStatus(), id);
    }
    public int delete(long id) { return jdbc.update("DELETE FROM suppliers WHERE id=?", id); }
    public int count() { return jdbc.queryForObject("SELECT COUNT(*) FROM suppliers", Integer.class); }
}
