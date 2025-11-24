-- Script para adicionar coluna role à tabela hospedes existente
-- Execute este script se a tabela já existir sem a coluna role

ALTER TABLE hospedes ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'HOSPEDE';

-- Atualizar todos os registros existentes para ter role HOSPEDE
UPDATE hospedes SET role = 'HOSPEDE' WHERE role IS NULL OR role = '';

-- Inserir usuário admin (senha "admin123" com hash BCrypt)
INSERT INTO hospedes (nome, email, telefone, senha, data_nascimento, cpf, role)
VALUES (
    'Admin do Sistema',
    'admin@lodgfy.com',
    '(11) 99999-9999',
    '$2a$10$N9qo8uLOickgx2ZrVzaKe.4rjxiTOGgLwTGk4z4g4Xqc9vY8eG8lO', -- senha: admin123
    '1990-01-01',
    '00000000000',
    'ADMIN'
)
ON DUPLICATE KEY UPDATE
    nome = VALUES(nome),
    email = VALUES(email),
    role = VALUES(role);
