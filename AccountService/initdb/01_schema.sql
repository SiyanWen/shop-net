
-- Create roles table and insert default roles
CREATE TABLE IF NOT EXISTS roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(60) NOT NULL UNIQUE
);

-- Insert default roles
INSERT INTO roles (name) VALUES ('ROLE_ADMIN') ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name) VALUES ('ROLE_USER') ON CONFLICT (name) DO NOTHING;

-- Create users table if it doesn't exist
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL,
    shipping_address VARCHAR(255) NOT NULL,
    billing_address VARCHAR(255) NOT NULL,
    payment_method VARCHAR(255) NOT NULL
);

-- Create user_roles join table
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
);


-- Insert users with role references
INSERT INTO users (email, username, password, enabled, shipping_address, billing_address, payment_method) VALUES
    ('abc@gmail.com', 'foo', '$2a$10$hashedpassword123456789012345678901234567890', true, '111 S Main St. LA CA 90000', '123 Fifth St. LA CA 90000', 'card'),
    ('789@admin.com', 'admin_a', '$2a$10$hashedpassword123456789012345678901234567890', true, '111 S Main St. LA CA 90000', '123 Fifth St. LA CA 90000', 'paypal'),
    ('john.doe@example.com', 'john_doe', '$2a$10$hashedpassword123456789012345678901234567890', true, '456 Oak Ave. NYC NY 10001', '456 Oak Ave. NYC NY 10001', 'card'),
    ('jane.smith@example.com', 'jane_smith', '$2a$10$hashedpassword123456789012345678901234567890', true, '789 Pine Rd. Chicago IL 60601', '789 Pine Rd. Chicago IL 60601', 'paypal'),
    ('bob.wilson@example.com', 'bob_wilson', '$2a$10$hashedpassword123456789012345678901234567890', true, '321 Elm Blvd. Houston TX 77001', '321 Elm Blvd. Houston TX 77001', 'card')
ON CONFLICT (email) DO NOTHING;

-- Associate users with roles
-- User 'foo' gets ROLE_USER
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.email = 'abc@gmail.com' AND r.name = 'ROLE_USER'
ON CONFLICT DO NOTHING;

-- User 'admin_a' gets ROLE_ADMIN
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.email = '789@admin.com' AND r.name = 'ROLE_ADMIN'
ON CONFLICT DO NOTHING;

-- User 'john_doe' gets ROLE_USER
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.email = 'john.doe@example.com' AND r.name = 'ROLE_USER'
ON CONFLICT DO NOTHING;

-- User 'jane_smith' gets ROLE_USER
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.email = 'jane.smith@example.com' AND r.name = 'ROLE_USER'
ON CONFLICT DO NOTHING;

-- User 'bob_wilson' gets ROLE_USER
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.email = 'bob.wilson@example.com' AND r.name = 'ROLE_USER'
ON CONFLICT DO NOTHING;