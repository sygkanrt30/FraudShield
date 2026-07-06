CREATE TABLE client
(
    id        UUID PRIMARY KEY,
    email     VARCHAR(150) NOT NULL UNIQUE,
    full_name VARCHAR(200) NOT NULL
);

CREATE TABLE transaction_record
(
    transaction_id UUID PRIMARY KEY,
    from_client_id UUID           NOT NULL,
    to_client_id   UUID           NOT NULL,
    amount         NUMERIC(19, 4) NOT NULL,
    status         VARCHAR(30)    NOT NULL,
    currency       VARCHAR(3)     NOT NULL,
    created_at      TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,


    CONSTRAINT fk_from_client FOREIGN KEY (from_client_id) REFERENCES client (id),
    CONSTRAINT fk_to_client FOREIGN KEY (to_client_id) REFERENCES client (id)
);

CREATE INDEX idx_tr_from_client ON transaction_record (from_client_id);
CREATE INDEX idx_tr_to_client ON transaction_record (to_client_id);
CREATE INDEX idx_id ON transaction_record(transaction_id);
CREATE INDEX idx_status ON transaction_record(status);