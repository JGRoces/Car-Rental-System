-- ============================================================
--   Car Rental System — Database Schema
--   Database : car_rental_db
--   Version  : 2.0
--   Updated  : Aligned with final project file structure
-- ============================================================

-- Create and select the database
CREATE DATABASE IF NOT EXISTS car_rental_db;
USE car_rental_db;

-- ============================================================
--   TABLE: users
--   Base account table for both Admin and Customer actors.
--   Mirrors User.java (pckModels)
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
    user_id     INT             AUTO_INCREMENT PRIMARY KEY,
    full_name   VARCHAR(100)    NOT NULL,
    email       VARCHAR(100)    NOT NULL UNIQUE,
    password    VARCHAR(255)    NOT NULL,                       -- Store hashed passwords later
    role        ENUM('ADMIN', 'CUSTOMER') NOT NULL,
    created_at  TIMESTAMP       DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
--   TABLE: customers
--   Extended profile info for Customer accounts only.
--   Mirrors Customer.java (pckModels)
--   Linked to users via user_id (one-to-one)
-- ============================================================
CREATE TABLE IF NOT EXISTS customers (
    customer_id     INT             AUTO_INCREMENT PRIMARY KEY,
    user_id         INT             NOT NULL UNIQUE,
    phone_number    VARCHAR(20),
    address         VARCHAR(255),
    license_number  VARCHAR(50)     NOT NULL UNIQUE,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

-- ============================================================
--   TABLE: cars
--   Car inventory managed by Admin.
--   Mirrors Car.java (pckModels)
-- ============================================================
CREATE TABLE IF NOT EXISTS cars (
    car_id          INT             AUTO_INCREMENT PRIMARY KEY,
    brand           VARCHAR(50)     NOT NULL,
    model           VARCHAR(50)     NOT NULL,
    year            YEAR            NOT NULL,
    plate_number    VARCHAR(20)     NOT NULL UNIQUE,
    category        ENUM('Sedan', 'SUV', 'Van', 'Truck')       NOT NULL,
    daily_rate      DECIMAL(10, 2)  NOT NULL,
    status          ENUM('AVAILABLE', 'RENTED', 'MAINTENANCE') DEFAULT 'AVAILABLE',
    created_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
--   TABLE: rentals
--   Tracks all rental transactions between Customer and Car.
--   Mirrors Rental.java (pckModels)
-- ============================================================
CREATE TABLE IF NOT EXISTS rentals (
    rental_id       INT             AUTO_INCREMENT PRIMARY KEY,
    customer_id     INT             NOT NULL,
    car_id          INT             NOT NULL,
    start_date      DATE            NOT NULL,
    end_date        DATE            NOT NULL,
    total_amount    DECIMAL(10, 2)  DEFAULT 0.00,              -- Calculated by RentalService.java
    status          ENUM('PENDING', 'ACTIVE', 'COMPLETED', 'CANCELLED') DEFAULT 'PENDING',
    created_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    FOREIGN KEY (car_id) REFERENCES cars(car_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);

-- ============================================================
--   TABLE: payments
--   Payment records linked to a rental.
--   Mirrors Payment.java (pckModels)
-- ============================================================
CREATE TABLE IF NOT EXISTS payments (
    payment_id      INT             AUTO_INCREMENT PRIMARY KEY,
    rental_id       INT             NOT NULL,
    amount_paid     DECIMAL(10, 2)  NOT NULL,
    payment_date    TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    payment_method  ENUM('CASH', 'CARD', 'ONLINE') NOT NULL,
    status          ENUM('PAID', 'PENDING', 'REFUNDED') DEFAULT 'PENDING',
    FOREIGN KEY (rental_id) REFERENCES rentals(rental_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);

-- ============================================================
--   SAMPLE DATA — For testing only
--   Remove or replace before final submission
-- ============================================================

-- Default Admin account
-- ⚠️ Password is plain text for testing only — hash it in production
INSERT INTO users (full_name, email, password, role)
VALUES ('Administrator', 'admin@carrental.com', 'admin123', 'ADMIN');

-- Sample Customer account
INSERT INTO users (full_name, email, password, role)
VALUES ('Juan Dela Cruz', 'juan@email.com', 'customer123', 'CUSTOMER');

-- Link the sample customer to the customers table
INSERT INTO customers (user_id, phone_number, address, license_number)
VALUES (2, '09171234567', '123 Rizal St, Manila', 'N01-23-456789');

-- Sample Cars
INSERT INTO cars (brand, model, year, plate_number, category, daily_rate, status)
VALUES
    ('Toyota',      'Vios',       2022, 'AAA-1234', 'Sedan', 1500.00, 'AVAILABLE'),
    ('Honda',       'CR-V',       2023, 'BBB-5678', 'SUV',   2500.00, 'AVAILABLE'),
    ('Mitsubishi',  'L300',       2021, 'CCC-9012', 'Van',   3000.00, 'AVAILABLE'),
    ('Ford',        'Ranger',     2022, 'DDD-3456', 'Truck', 3500.00, 'AVAILABLE'),
    ('Toyota',      'Fortuner',   2023, 'EEE-7890', 'SUV',   4000.00, 'MAINTENANCE');

-- ============================================================
--   VERIFY — Uncomment and run these to confirm setup
-- ============================================================
-- SELECT * FROM users;
-- SELECT * FROM customers;
-- SELECT * FROM cars;
-- SELECT * FROM rentals;
-- SELECT * FROM payments;
