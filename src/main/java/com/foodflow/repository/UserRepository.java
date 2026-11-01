package com.foodflow.repository;

import com.foodflow.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository {
    private final JdbcTemplate jdbc;

    public UserRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<User> mapper = (rs, rowNum) -> {
        Timestamp ts = rs.getTimestamp("created_at");
        return new User(
            rs.getLong("id"),
            rs.getString("full_name"),
            rs.getString("email"),
            rs.getString("phone"),
            rs.getString("password"),
            rs.getString("role"),
            rs.getString("status"),
            ts == null ? null : ts.toLocalDateTime()
        );
    };

    public Optional<User> findByEmail(String email) {
        if (email == null) return Optional.empty();
        List<User> results = jdbc.query("SELECT * FROM users WHERE LOWER(email) = LOWER(?)", mapper, email.trim());
        return results.stream().findFirst();
    }

    public Optional<User> findById(long id) {
        List<User> results = jdbc.query("SELECT * FROM users WHERE id = ?", mapper, id);
        return results.stream().findFirst();
    }

    public boolean existsByEmail(String email) {
        if (email == null) return false;
        Integer count = jdbc.queryForObject(
            "SELECT COUNT(*) FROM users WHERE LOWER(email) = LOWER(?)",
            Integer.class,
            email.trim()
        );
        return count != null && count > 0;
    }

    public User save(User user) {
        jdbc.update(
            "INSERT INTO users(full_name, email, password, phone, role, status) VALUES(?, LOWER(?), ?, ?, ?, ?)",
            user.getFullName(),
            user.getEmail().trim(),
            user.getPassword(),
            user.getPhone(),
            user.getRole(),
            user.getStatus()
        );
        Long id = jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        return findById(id == null ? 0 : id).orElseThrow();
    }

    public int count() {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
        return count == null ? 0 : count;
    }
}
