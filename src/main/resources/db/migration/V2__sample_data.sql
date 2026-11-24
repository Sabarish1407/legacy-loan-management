-- V2__sample_data.sql: Sample Development and Test Seed Data

-- Insert sample customers
INSERT INTO customers (first_name, last_name, email, phone, national_id, address, date_of_birth, monthly_income, employment_type, credit_score, kyc_status)
VALUES 
('John', 'Doe', 'john.doe@example.com', '+1-555-0100', 'NAT-ID-10001', '123 Main St, New York, NY', '1985-05-15', 75000.00, 'SALARIED', 750, 'VERIFIED'),
('Jane', 'Smith', 'jane.smith@example.com', '+1-555-0200', 'NAT-ID-10002', '456 Oak Ave, San Francisco, CA', '1990-08-22', 95000.00, 'SALARIED', 780, 'VERIFIED'),
('Robert', 'Johnson', 'robert.j@example.com', '+1-555-0300', 'NAT-ID-10003', '789 Pine Rd, Chicago, IL', '1978-12-01', 35000.00, 'SELF_EMPLOYED', 620, 'PENDING');

-- Insert default application users (password: 'password' hashed with BCrypt)
-- Hash: $2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG
INSERT INTO app_users (username, password, role, customer_id, enabled)
VALUES 
('admin', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'ADMIN', NULL, TRUE),
('officer', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'LOAN_OFFICER', NULL, TRUE),
('john_doe', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'CUSTOMER', 1, TRUE),
('jane_smith', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'CUSTOMER', 2, TRUE),
('robert_j', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'CUSTOMER', 3, TRUE);
