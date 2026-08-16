CREATE TABLE verification_token
(
    id          UUID PRIMARY KEY,
    user_id     UUID         NOT NULL REFERENCES app_user (id),
    type        VARCHAR(30)  NOT NULL
        CHECK (type IN ('EMAIL_VERIFICATION', 'PASSWORD_RESET', 'EMAIL_CHANGE')),
    token_hash  VARCHAR(255) NOT NULL,
    payload     TEXT,
    expires_at  TIMESTAMPTZ  NOT NULL,
    consumed_at TIMESTAMPTZ,
    created_at  TIMESTAMPTZ  NOT NULL,
    created_by  VARCHAR(255) NOT NULL,
    updated_at  TIMESTAMPTZ  NOT NULL,
    updated_by  VARCHAR(255) NOT NULL,

    CONSTRAINT uq_verification_token_hash UNIQUE (token_hash)
);

CREATE INDEX idx_verification_token_user_id ON verification_token (user_id);