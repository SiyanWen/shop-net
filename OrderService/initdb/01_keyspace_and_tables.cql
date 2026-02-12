-- Keyspace (single node dev)
CREATE KEYSPACE IF NOT EXISTS shop_ks
    WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};

USE shop_ks;

-- Query tables (from earlier design)
CREATE TABLE IF NOT EXISTS orders_by_id (
                                            order_id uuid PRIMARY KEY,
                                            user_id uuid,
                                            created_at timestamp,
                                            status text,
                                            currency text,
                                            items list<frozen<tuple<text, int, decimal>>>,
                                            subtotal decimal,
                                            tax decimal,
                                            shipping_fee decimal,
                                            total decimal,
                                            shipping_address text,
                                            billing_address text,
                                            payment_ref text,
                                            attributes map<text, text>
);

CREATE TABLE IF NOT EXISTS orders_by_user (
                                              user_id uuid,
                                              created_at timestamp,
                                              order_id uuid,
                                              status text,
                                              total decimal,
                                              currency text,
                                              PRIMARY KEY ((user_id), created_at, order_id)
) WITH CLUSTERING ORDER BY (created_at DESC, order_id ASC);

-- Seed data
INSERT INTO orders_by_id (order_id, user_id, created_at, status, currency, items, subtotal, tax, shipping_fee, total, shipping_address, billing_address, payment_ref, attributes)
VALUES (
    11111111-1111-1111-1111-111111111111,
    aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa,
    '2025-06-01T10:30:00Z',
    'DELIVERED',
    'USD',
    [('Wireless Mouse', 2, 25.99), ('USB-C Hub', 1, 45.00)],
    96.98,
    7.76,
    5.99,
    110.73,
    '123 Main St, New York, NY 10001',
    '123 Main St, New York, NY 10001',
    'PAY-001-ABC',
    {'source': 'web', 'coupon': 'SAVE10'}
);

INSERT INTO orders_by_id (order_id, user_id, created_at, status, currency, items, subtotal, tax, shipping_fee, total, shipping_address, billing_address, payment_ref, attributes)
VALUES (
    22222222-2222-2222-2222-222222222222,
    aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa,
    '2025-07-15T14:00:00Z',
    'SHIPPED',
    'USD',
    [('Mechanical Keyboard', 1, 89.99)],
    89.99,
    7.20,
    0.00,
    97.19,
    '123 Main St, New York, NY 10001',
    '123 Main St, New York, NY 10001',
    'PAY-002-DEF',
    {'source': 'mobile'}
);

INSERT INTO orders_by_id (order_id, user_id, created_at, status, currency, items, subtotal, tax, shipping_fee, total, shipping_address, billing_address, payment_ref, attributes)
VALUES (
    33333333-3333-3333-3333-333333333333,
    bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb,
    '2025-08-20T09:15:00Z',
    'PENDING',
    'USD',
    [('Monitor Stand', 1, 34.50), ('Webcam HD', 1, 59.99), ('Desk Lamp', 2, 22.00)],
    138.49,
    11.08,
    5.99,
    155.56,
    '456 Oak Ave, San Francisco, CA 94102',
    '456 Oak Ave, San Francisco, CA 94102',
    'PAY-003-GHI',
    {'source': 'web', 'gift_wrap': 'true'}
);

-- Denormalized rows for orders_by_user
INSERT INTO orders_by_user (user_id, created_at, order_id, status, total, currency)
VALUES (aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa, '2025-06-01T10:30:00Z', 11111111-1111-1111-1111-111111111111, 'DELIVERED', 110.73, 'USD');

INSERT INTO orders_by_user (user_id, created_at, order_id, status, total, currency)
VALUES (aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa, '2025-07-15T14:00:00Z', 22222222-2222-2222-2222-222222222222, 'SHIPPED', 97.19, 'USD');

INSERT INTO orders_by_user (user_id, created_at, order_id, status, total, currency)
VALUES (bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb, '2025-08-20T09:15:00Z', 33333333-3333-3333-3333-333333333333, 'PENDING', 155.56, 'USD');
