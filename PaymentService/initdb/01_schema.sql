CREATE TABLE payments (
    payment_id       UUID PRIMARY KEY,
    order_id         UUID NOT NULL,
    amount           NUMERIC(12,2) NOT NULL,
    currency         VARCHAR(3) DEFAULT 'USD',
    status           VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    payment_method   VARCHAR(30),
    idempotency_key  VARCHAR(64) UNIQUE NOT NULL,
    created_at       TIMESTAMP DEFAULT NOW(),
    updated_at       TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_payments_order_id ON payments(order_id);
CREATE INDEX idx_payments_idempotency_key ON payments(idempotency_key);
