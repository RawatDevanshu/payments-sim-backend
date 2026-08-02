CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    role VARCHAR(255), -- nullable: not enforced on the entity 
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE wallets (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    upi_handle VARCHAR(255) NOT NULL UNIQUE,
    balance NUMERIC(19,2) NOT NULL,
    is_active BOOLEAN NOT NULL
);

CREATE TABLE bankaccounts (
   id BIGSERIAL PRIMARY KEY,
   user_id BIGINT NOT NULL REFERENCES users(id),
   account_number VARCHAR(255) NOT NULL UNIQUE,
   balance NUMERIC(19,2) NOT NULL,
   bank_pin_hash VARCHAR(255) NOT NULL,
   created_at TIMESTAMP NOT NULL
);

CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    transaction_id VARCHAR(255) NOT NULL UNIQUE,
    from_wallet_id BIGINT REFERENCES wallets(id),
    to_wallet_id BIGINT REFERENCES wallets(id),
    from_bank_account_id BIGINT REFERENCES bankaccounts(id),
    to_bank_account_id BIGINT REFERENCES bankaccounts(id),
    amount NUMERIC(19,2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    type VARCHAR(50) NOT NULL,
    remarks VARCHAR(255),
    timestamp TIMESTAMP NOT NULL
);

CREATE TABLE idempotency_keys (
    id BIGSERIAL PRIMARY KEY,
    idempotency_key VARCHAR(36) NOT NULL,
    user_id BIGINT NOT NULL REFERENCES users(id),
    endpoint VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL,
    response_body TEXT,
    http_status_code INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL,
    -- matches the entity's unique constraint on (idempotency_key, user_id)
    CONSTRAINT uk_idempotency_key_user UNIQUE (idempotency_key, user_id)
);

CREATE TABLE upipins (
    id BIGSERIAL PRIMARY KEY,
    wallet_id BIGINT NOT NULL REFERENCES wallets(id),
    pin_hash VARCHAR(255) NOT NULL,
    is_locked BOOLEAN NOT NULL,
    failed_attempts INTEGER NOT NULL
);

CREATE INDEX idx_transactions_status ON transactions(status);
CREATE INDEX idx_transactions_from_wallet ON transactions(from_wallet_id);
CREATE INDEX idx_transactions_to_wallet ON transactions(to_wallet_id);
CREATE INDEX idx_idempotency_keys_status ON idempotency_keys(status);

ALTER TABLE wallets ADD CONSTRAINT chk_wallet_balance_non_negative CHECK (balance >=0);