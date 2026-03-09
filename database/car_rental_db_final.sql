-- ============================================================
--  Car Rental System — Final Canonical Schema
--  File    : car_rental_db_final.sql
--  Version : 4.0
--  Notes   : Merged from all 3 SQL files. Matches all DAO
--            and Model classes exactly. Fully normalized (3NF).
-- ============================================================


-- ============================================================
--  1. DATABASE
-- ============================================================
DROP DATABASE IF EXISTS car_rental_db;
CREATE DATABASE car_rental_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE car_rental_db;


-- ============================================================
--  2. APP USER
--  Java app connects as carrentaluser — never as root.
-- ============================================================
DROP USER IF EXISTS 'carrentaluser'@'localhost';
CREATE USER 'carrentaluser'@'localhost' IDENTIFIED BY 'carrentalpass';
GRANT ALL PRIVILEGES ON car_rental_db.* TO 'carrentaluser'@'localhost';
FLUSH PRIVILEGES;


-- ============================================================
--  3. TABLES
-- ============================================================

-- ------------------------------------------------------------
--  users
--  Base account for ALL actors: ADMIN, CUSTOMER, DRIVER.
--  role determines which dashboard is shown after login.
-- ------------------------------------------------------------
CREATE TABLE users (
    user_id    INT            AUTO_INCREMENT PRIMARY KEY,
    full_name  VARCHAR(100)   NOT NULL,
    email      VARCHAR(100)   NOT NULL UNIQUE,
    password   VARCHAR(255)   NOT NULL,
    role       ENUM('ADMIN','CUSTOMER','DRIVER') NOT NULL,
    created_at TIMESTAMP      DEFAULT CURRENT_TIMESTAMP
);

-- ------------------------------------------------------------
--  customers
--  Extended profile for CUSTOMER accounts only.
--  One-to-one with users via user_id.
--  Matches: Customer.java, CustomerDAO.java
-- ------------------------------------------------------------
CREATE TABLE customers (
    customer_id INT          AUTO_INCREMENT PRIMARY KEY,
    user_id     INT          NOT NULL UNIQUE,
    phone       VARCHAR(20),
    photo_path  VARCHAR(500),
    CONSTRAINT fk_customers_user
        FOREIGN KEY (user_id) REFERENCES users(user_id)
        ON DELETE CASCADE ON UPDATE CASCADE
);

-- ------------------------------------------------------------
--  drivers
--  Extended profile for DRIVER accounts only.
--  One-to-one with users via user_id.
--  Requires admin verification before receiving bookings.
--  Matches: Driver.java, DriverDAO.java
-- ------------------------------------------------------------
CREATE TABLE drivers (
    driver_id      INT          AUTO_INCREMENT PRIMARY KEY,
    user_id        INT          NOT NULL UNIQUE,
    phone          VARCHAR(20),
    license_number VARCHAR(50)  NOT NULL UNIQUE,
    license_expiry DATE         NOT NULL,
    vehicle_type   VARCHAR(50),
    status         ENUM('PENDING','VERIFIED','REJECTED') DEFAULT 'PENDING',
    photo_path     VARCHAR(500),
    verified_at    TIMESTAMP    NULL,
    CONSTRAINT fk_drivers_user
        FOREIGN KEY (user_id) REFERENCES users(user_id)
        ON DELETE CASCADE ON UPDATE CASCADE
);

-- ------------------------------------------------------------
--  cars
--  Vehicle inventory managed by Admin.
--  Matches: Car.java, CarDAO.java (all columns used in mapRow)
-- ------------------------------------------------------------
CREATE TABLE cars (
    car_id        INT             AUTO_INCREMENT PRIMARY KEY,
    brand         VARCHAR(50)     NOT NULL,
    model         VARCHAR(50)     NOT NULL,
    year          YEAR            NOT NULL,
    plate_number  VARCHAR(20)     NOT NULL UNIQUE,
    category      ENUM('Sedan','SUV','Van','Truck','Pickup','Coupe','Minivan','MPV') NOT NULL,
    transmission  ENUM('Automatic','Manual','CVT') NOT NULL DEFAULT 'Automatic',
    fuel_type     ENUM('Gasoline','Diesel','Hybrid','Electric') NOT NULL DEFAULT 'Gasoline',
    seat_capacity INT             NOT NULL DEFAULT 5,
    daily_rate    DECIMAL(10,2)   NOT NULL,
    status        ENUM('AVAILABLE','RENTED','MAINTENANCE') DEFAULT 'AVAILABLE',
    image_path    VARCHAR(255),
    color         VARCHAR(30),
    created_at    TIMESTAMP       DEFAULT CURRENT_TIMESTAMP
);

-- ------------------------------------------------------------
--  rentals
--  Rental transactions linking Customer, Car, and optionally Driver.
--  total_amount calculated by RentalService (days x daily_rate).
--  Matches: Rental.java, RentalDAO.java
-- ------------------------------------------------------------
CREATE TABLE rentals (
    rental_id    INT           AUTO_INCREMENT PRIMARY KEY,
    customer_id  INT           NOT NULL,
    car_id       INT           NOT NULL,
    driver_id    INT           NULL,
    start_date   DATE          NOT NULL,
    end_date     DATE          NOT NULL,
    total_amount DECIMAL(10,2) DEFAULT 0.00,
    status       ENUM('PENDING','ACTIVE','COMPLETED','CANCELLED') DEFAULT 'PENDING',
    created_at   TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_rentals_customer
        FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_rentals_car
        FOREIGN KEY (car_id) REFERENCES cars(car_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_rentals_driver
        FOREIGN KEY (driver_id) REFERENCES drivers(driver_id)
        ON DELETE SET NULL ON UPDATE CASCADE
);

-- ------------------------------------------------------------
--  payments
--  One payment record per rental.
--  Matches: Payment.java
-- ------------------------------------------------------------
CREATE TABLE payments (
    payment_id     INT           AUTO_INCREMENT PRIMARY KEY,
    rental_id      INT           NOT NULL,
    amount_paid    DECIMAL(10,2) NOT NULL,
    payment_date   TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    payment_method ENUM('CASH','CARD','ONLINE') NOT NULL,
    status         ENUM('PAID','PENDING','REFUNDED') DEFAULT 'PENDING',
    CONSTRAINT fk_payments_rental
        FOREIGN KEY (rental_id) REFERENCES rentals(rental_id)
        ON DELETE RESTRICT ON UPDATE CASCADE
);


-- ============================================================
--  4. SAMPLE DATA
-- ============================================================

-- Admin
INSERT INTO users (full_name, email, password, role)
VALUES ('Administrator', 'admin@carrental.com', 'admin123', 'ADMIN');

-- Customers
INSERT INTO users (full_name, email, password, role) VALUES
('Juan Dela Cruz', 'juan@email.com',   'customer123', 'CUSTOMER'),
('Maria Santos',   'maria@email.com',  'customer123', 'CUSTOMER'),
('Carlos Reyes',   'carlos@email.com', 'customer123', 'CUSTOMER');

INSERT INTO customers (user_id, phone, photo_path) VALUES
(2, '09171234567', NULL),
(3, '09281234567', NULL),
(4, '09391234567', NULL);

-- Drivers
INSERT INTO users (full_name, email, password, role) VALUES
('Pedro Santos',  'pedro@email.com',  'driver123', 'DRIVER'),
('Rosa Mendoza',  'rosa@email.com',   'driver123', 'DRIVER');

INSERT INTO drivers (user_id, phone, license_number, license_expiry, vehicle_type, status, photo_path) VALUES
(5, '09181234567', 'N01-23-456789', '2027-06-15', 'Sedan',  'VERIFIED', NULL),
(6, '09291234567', 'N01-23-999888', '2026-12-01', 'SUV',    'PENDING',  NULL);

-- Cars
INSERT INTO cars (brand, model, year, plate_number, category, transmission, fuel_type, seat_capacity, daily_rate, status, image_path, color) VALUES
('Toyota',     'VIOS',     2024, 'ABC-1234', 'Sedan',  'Automatic', 'Gasoline', 5, 1600.00, 'AVAILABLE',   'Toyota VIOS - 2024 (Red).jpg',         'Red'),
('Toyota',     'Hilux',    2022, 'DEF-5678', 'Pickup', 'Manual',    'Diesel',   5, 1800.00, 'AVAILABLE',   'Toyota Hilux - 2022 (White).jpg',      'White'),
('Toyota',     'Raize',    2022, 'GHI-9012', 'SUV',    'Automatic', 'Gasoline', 5, 1400.00, 'AVAILABLE',   'Toyota Raize - 2022 (Black).jpg',      'Black'),
('Toyota',     'Innova',   2023, 'JKL-3456', 'Van',    'Automatic', 'Diesel',   7, 2200.00, 'AVAILABLE',   'Toyota Innova - 2023 (White).jpg',     'White'),
('Toyota',     'Fortuner', 2023, 'MNO-7890', 'SUV',    'Automatic', 'Diesel',   7, 2800.00, 'RENTED',      'Toyota Fortuner - 2023 (White).jpg',   'White'),
('Honda',      'City',     2022, 'PQR-1122', 'Sedan',  'Automatic', 'Gasoline', 5, 1500.00, 'AVAILABLE',   'Honda City - 2022 (Silver).jpg',       'Silver'),
('Honda',      'City',     2026, 'PQR-1726', 'Sedan',  'Automatic', 'Gasoline', 5, 1700.00, 'AVAILABLE',   'Honda City - 2026 (Red).jpg',          'Red'),
('Honda',      'BR-V',     2023, 'STU-3344', 'SUV',    'Automatic', 'Gasoline', 7, 1900.00, 'AVAILABLE',   'Honda BR-V - 2023 (Gray).jpg',         'Gray'),
('Honda',      'CR-V',     2022, 'VWX-5566', 'SUV',    'Automatic', 'Gasoline', 5, 2400.00, 'MAINTENANCE', 'Honda CR-V - 2022 (White).jpg',        'White'),
('Mitsubishi', 'Mirage',   2022, 'YZA-7788', 'Sedan',  'Automatic', 'Gasoline', 5, 1200.00, 'AVAILABLE',   'Mitsubishi Mirage - 2022 (White).jpg', 'White'),
('Mitsubishi', 'Strada',   2023, 'BCD-9900', 'Pickup', 'Manual',    'Diesel',   5, 2000.00, 'AVAILABLE',   'Mitsubishu Strada - 2023 (Brown).jpg', 'Brown'),
('Mitsubishi', 'Montero',  2023, 'EFG-1111', 'SUV',    'Automatic', 'Diesel',   7, 2600.00, 'AVAILABLE',   'Mitsubishi Montero - 2023 (White).jpg','White'),
('BYD',        'Atto 3',   2024, 'HIJ-2222', 'SUV',    'Automatic', 'Electric', 5, 2200.00, 'AVAILABLE',   'BYD Atto 3 - 2024 (Gray).jpg',         'Gray'),
('BYD',        'Seal',     2024, 'KLM-3333', 'Sedan',  'Automatic', 'Electric', 5, 2500.00, 'AVAILABLE',   'BYD Seal - 2024 (White).jpg',          'White');

-- Sample rentals
INSERT INTO rentals (customer_id, car_id, driver_id, start_date, end_date, total_amount, status) VALUES
(1, 5, 1, '2026-03-01', '2026-03-05', 11200.00, 'COMPLETED'),
(2, 1, NULL, '2026-03-10', '2026-03-12', 3200.00, 'ACTIVE'),
(3, 10, NULL, '2026-03-15', '2026-03-18', 3600.00, 'PENDING');

-- Sample payments
INSERT INTO payments (rental_id, amount_paid, payment_method, status) VALUES
(1, 11200.00, 'CASH',   'PAID'),
(2,  3200.00, 'CARD',   'PAID'),
(3,  3600.00, 'ONLINE', 'PENDING');


-- ============================================================
--  5. VERIFY
--  Expected: users=6, customers=3, drivers=2, cars=14,
--            rentals=3, payments=3
-- ============================================================
SELECT 'users'     AS table_name, COUNT(*) AS row_count FROM users
UNION ALL SELECT 'customers',  COUNT(*) FROM customers
UNION ALL SELECT 'drivers',    COUNT(*) FROM drivers
UNION ALL SELECT 'cars',       COUNT(*) FROM cars
UNION ALL SELECT 'rentals',    COUNT(*) FROM rentals
UNION ALL SELECT 'payments',   COUNT(*) FROM payments;
