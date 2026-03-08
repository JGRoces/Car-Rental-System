-- ============================================================
--   Car Rental System — Database Schema
--   Database : car_rental_db
--   Version  : 3.0
--   Updated  : Added drivers table, fixed customers table,
--              added DRIVER role, photo_path columns
-- ============================================================

CREATE DATABASE IF NOT EXISTS car_rental_db;
USE car_rental_db;

-- ============================================================
--   DROP TABLES in reverse FK order before recreating.
--   Run this to reset the schema cleanly after any changes.
-- ============================================================
DROP TABLE IF EXISTS payments;
DROP TABLE IF EXISTS rentals;
DROP TABLE IF EXISTS cars;
DROP TABLE IF EXISTS drivers;
DROP TABLE IF EXISTS customers;
DROP TABLE IF EXISTS users;

-- ============================================================
--   TABLE: users
--   Base account for ALL actors — Admin, Customer, Driver.
--   Mirrors User.java (pckModels)
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
    user_id     INT          AUTO_INCREMENT PRIMARY KEY,
    full_name   VARCHAR(100) NOT NULL,
    email       VARCHAR(100) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,          -- store hashed in production
    role        ENUM('ADMIN','CUSTOMER','DRIVER') NOT NULL,
    created_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
--   TABLE: customers
--   Extended profile for Customer accounts.
--   Mirrors Customer.java (pckModels)
--   One-to-one with users via user_id.
-- ============================================================
CREATE TABLE IF NOT EXISTS customers (
    customer_id  INT          AUTO_INCREMENT PRIMARY KEY,
    user_id      INT          NOT NULL UNIQUE,
    phone        VARCHAR(20),
    photo_path   VARCHAR(500),                  -- path to profile photo, nullable
    FOREIGN KEY (user_id) REFERENCES users(user_id)
        ON DELETE CASCADE ON UPDATE CASCADE
);

-- ============================================================
--   TABLE: drivers
--   Extended profile for Driver accounts.
--   Mirrors Driver.java (pckModels)
--   One-to-one with users via user_id.
--   Drivers require admin verification before receiving bookings.
-- ============================================================
CREATE TABLE IF NOT EXISTS drivers (
    driver_id        INT          AUTO_INCREMENT PRIMARY KEY,
    user_id          INT          NOT NULL UNIQUE,
    phone            VARCHAR(20),
    license_number   VARCHAR(50)  NOT NULL UNIQUE,
    license_expiry   DATE         NOT NULL,
    vehicle_type     VARCHAR(50),               -- preferred vehicle type
    status           ENUM('PENDING','VERIFIED','REJECTED') DEFAULT 'PENDING',
    photo_path       VARCHAR(500),              -- path to profile photo, nullable
    verified_at      TIMESTAMP    NULL,         -- set when admin verifies
    FOREIGN KEY (user_id) REFERENCES users(user_id)
        ON DELETE CASCADE ON UPDATE CASCADE
);

-- ============================================================
--   TABLE: cars
--   Car inventory managed by Admin.
--   Mirrors Car.java (pckModels)
-- ============================================================
CREATE TABLE IF NOT EXISTS cars (
    car_id       INT              AUTO_INCREMENT PRIMARY KEY,
    brand        VARCHAR(50)      NOT NULL,
    model        VARCHAR(50)      NOT NULL,
    year         YEAR             NOT NULL,
    plate_number VARCHAR(20)      NOT NULL UNIQUE,
    category     ENUM('Sedan','SUV','Van','Truck') NOT NULL,
    daily_rate   DECIMAL(10,2)    NOT NULL,
    status       ENUM('AVAILABLE','RENTED','MAINTENANCE') DEFAULT 'AVAILABLE',
    created_at   TIMESTAMP        DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
--   TABLE: rentals
--   Tracks rental transactions between Customer and Car.
--   Mirrors Rental.java (pckModels)
-- ============================================================
CREATE TABLE IF NOT EXISTS rentals (
    rental_id    INT             AUTO_INCREMENT PRIMARY KEY,
    customer_id  INT             NOT NULL,
    car_id       INT             NOT NULL,
    driver_id    INT             NULL,          -- optional assigned driver
    start_date   DATE            NOT NULL,
    end_date     DATE            NOT NULL,
    total_amount DECIMAL(10,2)   DEFAULT 0.00,
    status       ENUM('PENDING','ACTIVE','COMPLETED','CANCELLED') DEFAULT 'PENDING',
    created_at   TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (car_id)      REFERENCES cars(car_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (driver_id)   REFERENCES drivers(driver_id)
        ON DELETE SET NULL  ON UPDATE CASCADE
);

-- ============================================================
--   TABLE: payments
--   Payment records linked to a rental.
--   Mirrors Payment.java (pckModels)
-- ============================================================
CREATE TABLE IF NOT EXISTS payments (
    payment_id     INT              AUTO_INCREMENT PRIMARY KEY,
    rental_id      INT              NOT NULL,
    amount_paid    DECIMAL(10,2)    NOT NULL,
    payment_date   TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    payment_method ENUM('CASH','CARD','ONLINE') NOT NULL,
    status         ENUM('PAID','PENDING','REFUNDED') DEFAULT 'PENDING',
    FOREIGN KEY (rental_id) REFERENCES rentals(rental_id)
        ON DELETE RESTRICT ON UPDATE CASCADE
);

-- ============================================================
--   SAMPLE DATA — For testing only
--   Remove or replace before final submission
-- ============================================================

-- Admin account
INSERT INTO users (full_name, email, password, role)
VALUES ('Administrator', 'admin@carrental.com', 'admin123', 'ADMIN');

-- Sample Customer
INSERT INTO users (full_name, email, password, role)
VALUES ('Juan Dela Cruz', 'juan@email.com', 'customer123', 'CUSTOMER');

INSERT INTO customers (user_id, phone, photo_path)
VALUES (2, '09171234567', NULL);

-- Sample Driver (PENDING verification)
INSERT INTO users (full_name, email, password, role)
VALUES ('Pedro Santos', 'pedro@email.com', 'driver123', 'DRIVER');

INSERT INTO drivers (user_id, phone, license_number, license_expiry, vehicle_type, status, photo_path)
VALUES (3, '09181234567', 'N01-23-456789', '2027-06-15', 'Sedan', 'PENDING', NULL);

-- Sample Cars
INSERT INTO cars (brand, model, year, plate_number, category, daily_rate, status) VALUES
    ('Toyota',     'Vios',     2022, 'AAA-1234', 'Sedan', 1500.00, 'AVAILABLE'),
    ('Honda',      'CR-V',     2023, 'BBB-5678', 'SUV',   2500.00, 'AVAILABLE'),
    ('Mitsubishi', 'L300',     2021, 'CCC-9012', 'Van',   3000.00, 'AVAILABLE'),
    ('Ford',       'Ranger',   2022, 'DDD-3456', 'Truck', 3500.00, 'AVAILABLE'),
    ('Toyota',     'Fortuner', 2023, 'EEE-7890', 'SUV',   4000.00, 'MAINTENANCE');

-- ============================================================
--   VERIFY — Uncomment to confirm setup
-- ============================================================
-- SELECT * FROM users;
-- SELECT * FROM customers;
-- SELECT * FROM drivers;
-- SELECT * FROM cars;