-- Tabela de usuários. O email é único e a senha guarda só o hash BCrypt.
CREATE TABLE users (
    id        BIGSERIAL PRIMARY KEY,
    nome      VARCHAR(120) NOT NULL,
    email     VARCHAR(180) NOT NULL,
    senha     VARCHAR(100) NOT NULL,
    role      VARCHAR(20)  NOT NULL,
    criado_em TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_users_email UNIQUE (email)
);

-- Um usuário tem vários endereços. Se o usuário for apagado, os endereços vão junto.
CREATE TABLE addresses (
    id          BIGSERIAL PRIMARY KEY,
    logradouro  VARCHAR(200) NOT NULL,
    numero      VARCHAR(20)  NOT NULL,
    complemento VARCHAR(100),
    bairro      VARCHAR(100) NOT NULL,
    cidade      VARCHAR(100) NOT NULL,
    estado      VARCHAR(2)   NOT NULL,
    cep         VARCHAR(8)   NOT NULL,
    user_id     BIGINT       NOT NULL,
    CONSTRAINT fk_addresses_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_addresses_user_id ON addresses (user_id);
