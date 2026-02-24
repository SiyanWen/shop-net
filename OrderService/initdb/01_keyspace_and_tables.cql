-- Keyspace (single node dev)
CREATE KEYSPACE IF NOT EXISTS shop_ks
    WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};

USE shop_ks;

-- User-defined type for order items
CREATE TYPE IF NOT EXISTS order_item (
    item_id text,
    name text,
    quantity int,
    price decimal
);

-- Query tables
CREATE TABLE IF NOT EXISTS orders_by_id (
                                            order_id uuid PRIMARY KEY,
                                            user_id uuid,
                                            created_at timestamp,
                                            status text,
                                            currency text,
                                            items list<frozen<order_item>>,
                                            subtotal decimal,
                                            tax decimal,
                                            shipping_fee decimal,
                                            total decimal,
                                            shipping_address text,
                                            billing_address text,
                                            payment_ref text
);

CREATE TABLE IF NOT EXISTS orders_by_user (
                                              user_id uuid,
                                              created_at timestamp,
                                              order_id uuid,
                                              status text,
                                              total decimal,
                                              currency text,
                                              payment_ref text,
                                              PRIMARY KEY ((user_id), created_at, order_id)
) WITH CLUSTERING ORDER BY (created_at DESC, order_id ASC);


