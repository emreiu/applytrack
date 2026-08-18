CREATE TABLE refresh_token
(
    id           UUID PRIMARY KEY,
    user_id      UUID         NOT NULL REFERENCES app_user (id),
    token_hash   VARCHAR(255) NOT NULL,
    issued_at    TIMESTAMPTZ  NOT NULL,
    expires_at   TIMESTAMPTZ  NOT NULL,
    revoked_at   TIMESTAMPTZ,
    device_label VARCHAR(255),
    last_used_at TIMESTAMPTZ,
    ip_hash      VARCHAR(255),
    updated_at   TIMESTAMPTZ  NOT NULL,
    updated_by   VARCHAR(255) NOT NULL,

    CONSTRAINT uq_refresh_token_hash UNIQUE (token_hash)
);

CREATE INDEX idx_refresh_token_user_id ON refresh_token (user_id);

CREATE TABLE login_attempt
(
    id              UUID PRIMARY KEY,
    email_attempted VARCHAR(255) NOT NULL,
    user_id         UUID REFERENCES app_user (id),
    successful      BOOLEAN      NOT NULL,
    attempted_at    TIMESTAMPTZ  NOT NULL,
    ip_hash         VARCHAR(255),
    user_agent      VARCHAR(500)
);

CREATE INDEX idx_login_attempt_email_attempted ON login_attempt (email_attempted);
CREATE INDEX idx_login_attempt_user_id ON login_attempt (user_id);