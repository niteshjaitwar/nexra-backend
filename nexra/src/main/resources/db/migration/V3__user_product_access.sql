CREATE TABLE user_product_access (
    id              CHAR(36)        NOT NULL,
    user_id         CHAR(36)        NOT NULL,
    product         VARCHAR(30)     NOT NULL,
    product_role    VARCHAR(40)     NOT NULL,
    granted_at      TIMESTAMP       NOT NULL,
    granted_by      VARCHAR(36)     NULL,
    created_at      TIMESTAMP       NOT NULL,
    updated_at      TIMESTAMP       NOT NULL,
    created_by      VARCHAR(120)    NOT NULL,
    updated_by      VARCHAR(120)    NOT NULL,
    CONSTRAINT pk_user_product_access   PRIMARY KEY (id),
    CONSTRAINT fk_upa_user              FOREIGN KEY (user_id) REFERENCES user_accounts(id),
    CONSTRAINT uk_user_product          UNIQUE (user_id, product)
);

CREATE INDEX idx_upa_user_id ON user_product_access(user_id);
