-- ============================================================
--   Car Rental System — Database Schema
--   Database : car_rental_db
--   Version  : 3.0
--   Updated  : Added transmission, seat_capacity, image_path
--              to cars table. Updated category ENUM.
-- ============================================================

CREATE DATABASE IF NOT EXISTS car_rental_db;
USE car_rental_db;

-- ============================================================
--   Drop tables in reverse FK order (safe reset)
-- ============================================================
DROP TABLE IF EXISTS payments;
DROP TABLE IF EXISTS rentals;
DROP TABLE IF EXISTS cars;
DROP TABLE IF EXISTS customers;
DROP TABLE IF EXISTS users;

-- ============================================================
--   TABLE: users
-- ============================================================
CREATE TABLE users (
    user_id     INT          AUTO_INCREMENT PRIMARY KEY,
    full_name   VARCHAR(100) NOT NULL,
    email       VARCHAR(100) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    role        ENUM('ADMIN', 'CUSTOMER') NOT NULL,
    created_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
--   TABLE: customers
-- ============================================================
CREATE TABLE customers (
    customer_id    INT         AUTO_INCREMENT PRIMARY KEY,
    user_id        INT         NOT NULL UNIQUE,
    phone_number   VARCHAR(20),
    address        VARCHAR(255),
    license_number VARCHAR(50) NOT NULL UNIQUE,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

-- ============================================================
--   TABLE: cars
-- ============================================================
CREATE TABLE cars (
    car_id        INT            AUTO_INCREMENT PRIMARY KEY,
    brand         VARCHAR(50)    NOT NULL,
    model         VARCHAR(50)    NOT NULL,
    year          YEAR           NOT NULL,
    plate_number  VARCHARs(20)    NOT NULL UNIQUE,
    category      ENUM('Sedan','SUV','Van','Truck','Pickup','Coupe','Minivan') NOT NULL,
    transmission  ENUM('Automatic','Manual') NOT NULL DEFAULT 'Automatic',
    seat_capacity INT            NOT NULL DEFAULT 5,
    image_path    VARCHAR(255)   NULL,
    daily_rate    DECIMAL(10,2)  NOT NULL,
    status        ENUM('AVAILABLE','RENTED','MAINTENANCE') NOT NULL DEFAULT 'AVAILABLE',
    created_at    TIMESTAMP      DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
--   TABLE: rentals
-- ============================================================
CREATE TABLE rentals (
    rental_id    INT           AUTO_INCREMENT PRIMARY KEY,
    customer_id  INT           NOT NULL,
    car_id       INT           NOT NULL,
    start_date   DATE          NOT NULL,
    end_date     DATE          NOT NULL,
    total_amount DECIMAL(10,2) DEFAULT 0.00,
    status       ENUM('PENDING','ACTIVE','COMPLETED','CANCELLED') DEFAULT 'PENDING',
    created_at   TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (car_id) REFERENCES cars(car_id)
        ON DELETE RESTRICT ON UPDATE CASCADE
);

-- ============================================================
--   TABLE: payments
-- ============================================================
CREATE TABLE payments (
    payment_id     INT           AUTO_INCREMENT PRIMARY KEY,
    rental_id      INT           NOT NULL,
    amount_paid    DECIMAL(10,2) NOT NULL,
    payment_date   TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    payment_method ENUM('CASH','CARD','ONLINE') NOT NULL,
    status         ENUM('PAID','PENDING','REFUNDED') DEFAULT 'PENDING',
    FOREIGN KEY (rental_id) REFERENCES rentals(rental_id)
        ON DELETE RESTRICT ON UPDATE CASCADE
);

-- ============================================================
--   SAMPLE DATA
-- ============================================================

-- Admin account
INSERT INTO users (full_name, email, password, role)
VALUES ('Administrator', 'admin@carrental.com', 'admin123', 'ADMIN');

-- Customer account
INSERT INTO users (full_name, email, password, role)
VALUES ('Juan Dela Cruz', 'juan@email.com', 'customer123', 'CUSTOMER');

INSERT INTO customers (user_id, phone_number, address, license_number)
VALUES (2, '09171234567', '123 Rizal St, Manila', 'N01-23-456789');

-- Cars
INSERT INTO cars (brand, model, year, plate_number, category, transmission, seat_capacity, daily_rate, status, image_path) VALUES
-- Sedans
('Toyota',     'Vios',      2022, 'AAA-1234', 'Sedan',   'Automatic', 5,  1500.00, 'AVAILABLE',   NULL),
('Honda',      'Civic',     2023, 'BBB-5678', 'Sedan',   'Automatic', 5,  1800.00, 'AVAILABLE',   NULL),
('Mitsubishi', 'Lancer',    2020, 'CCC-9012', 'Sedan',   'Manual',    5,  1200.00, 'RENTED',      NULL),
('Toyota',     'Corolla',   2021, 'DDD-3456', 'Sedan',   'Automatic', 5,  1600.00, 'AVAILABLE',   NULL),
('Nissan',     'Almera',    2022, 'EEE-7890', 'Sedan',   'Automatic', 5,  1400.00, 'MAINTENANCE', NULL),
-- SUVs
('Honda',      'CR-V',      2023, 'FFF-2345', 'SUV',     'Automatic', 7,  2800.00, 'AVAILABLE',   NULL),
('Toyota',     'Fortuner',  2023, 'GGG-6789', 'SUV',     'Automatic', 7,  3800.00, 'AVAILABLE',   NULL),
('Ford',       'Everest',   2022, 'HHH-0123', 'SUV',     'Automatic', 7,  3200.00, 'RENTED',      NULL),
('Mitsubishi', 'Montero',   2021, 'III-4567', 'SUV',     'Automatic', 7,  3000.00, 'AVAILABLE',   NULL),
('Honda',      'BR-V',      2022, 'JJJ-8901', 'SUV',     'Automatic', 7,  2200.00, 'AVAILABLE',   NULL),
('Toyota',     'RAV4',      2023, 'KKK-2345', 'SUV',     'Automatic', 5,  3500.00, 'MAINTENANCE', NULL),
-- Vans
('Toyota',     'HiAce',     2022, 'LLL-6789', 'Van',     'Manual',    12, 3500.00, 'AVAILABLE',   NULL),
('Nissan',     'Urvan',     2021, 'MMM-0123', 'Van',     'Manual',    15, 3200.00, 'AVAILABLE',   NULL),
('Toyota',     'HiAce GL',  2023, 'NNN-4567', 'Van',     'Automatic', 10, 4000.00, 'RENTED',      NULL),
-- Trucks
('Mitsubishi', 'Strada',    2021, 'OOO-8901', 'Truck',   'Manual',    5,  2200.00, 'AVAILABLE',   NULL),
('Toyota',     'Hilux',     2022, 'PPP-2345', 'Truck',   'Manual',    5,  2500.00, 'AVAILABLE',   NULL),
('Ford',       'Ranger',    2023, 'QQQ-6789', 'Truck',   'Automatic', 5,  2800.00, 'RENTED',      NULL),
-- Pickups
('Nissan',     'Navara',    2021, 'RRR-0123', 'Pickup',  'Manual',    5,  2000.00, 'AVAILABLE',   NULL),
('Isuzu',      'D-Max',     2022, 'SSS-4567', 'Pickup',  'Automatic', 5,  2300.00, 'AVAILABLE',   NULL),
('Mazda',      'BT-50',     2022, 'TTT-8901', 'Pickup',  'Manual',    5,  2100.00, 'MAINTENANCE', NULL),
-- Coupes
('Honda',      'Civic RS',  2023, 'UUU-2345', 'Coupe',   'Automatic', 4,  2500.00, 'AVAILABLE',   NULL),
('Toyota',     'GR86',      2022, 'VVV-6789', 'Coupe',   'Manual',    4,  3000.00, 'AVAILABLE',   NULL),
-- Minivans
('Toyota',     'Innova',    2022, 'WWW-0123', 'Minivan', 'Automatic', 8,  2200.00, 'AVAILABLE',   NULL),
('Honda',      'Odyssey',   2021, 'XXX-4567', 'Minivan', 'Automatic', 8,  2600.00, 'RENTED',      NULL),
('Kia',        'Carnival',  2023, 'YYY-8901', 'Minivan', 'Automatic', 8,  2800.00, 'AVAILABLE',   NULL);

INSERT INTO cars (brand, model, year, plate_number, category, transmission, seat_capacity, daily_rate, status, image_path) VALUES
('Toyota',     'VIOS',      2026, 'ZZZ-1732', 'Sedan',   'Automatic', 5,  1600.00, 'AVAILABLE',   NULL);

-- ============================================================
--   VERIFY
-- ============================================================
SELECT * FROM users;
SELECT * FROM customers;
SELECT * FROM cars;