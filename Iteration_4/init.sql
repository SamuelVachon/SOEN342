-- Database initialization script for train_system
-- Run this to create the required tables

USE train_system;

-- Drop existing tables if they exist (for clean setup)
DROP TABLE IF EXISTS Reservation;
DROP TABLE IF EXISTS Trip;
DROP TABLE IF EXISTS Customer;

-- Create Customer table
CREATE TABLE Customer (
    customer_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    age INT NOT NULL,
    identifier VARCHAR(50) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create Trip table
CREATE TABLE Trip (
    trip_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    origin VARCHAR(100) NOT NULL,
    destination VARCHAR(100) NOT NULL,
    path_description TEXT,
    route VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES Customer(customer_id) ON DELETE CASCADE
);


-- Create Reservation table
CREATE TABLE Reservation (
    reservation_id INT AUTO_INCREMENT PRIMARY KEY,
    trip_id INT NOT NULL,
    passenger_name VARCHAR(100) NOT NULL,
    passenger_age INT NOT NULL,
    passenger_id VARCHAR(50) NOT NULL,
    ticket_number VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (trip_id) REFERENCES Trip(trip_id) ON DELETE CASCADE
);

-- Display the created tables
SHOW TABLES;

-- Display table structures
DESCRIBE Customer;
DESCRIBE Trip;
DESCRIBE Reservation;

SELECT 'Database tables initialized successfully!' AS Status;

