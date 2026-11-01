-- ========================================================
-- FoodFlow Migration V2: Add Users Table (Non-destructive)
-- Safe migration: preserves all existing tables and data.
-- ========================================================

CREATE TABLE IF NOT EXISTS users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  full_name VARCHAR(120) NOT NULL,
  email VARCHAR(150) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  phone VARCHAR(30) NULL,
  role VARCHAR(30) NOT NULL DEFAULT 'ROLE_USER',
  status ENUM('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
