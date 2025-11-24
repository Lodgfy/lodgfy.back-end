-- Script SQL para criar usuário admin
-- Execute este script no seu banco MySQL após criar as tabelas

-- Inserir um usuário admin (a senha será "admin123" com hash BCrypt)
INSERT INTO hospedes (nome, email, telefone, senha, data_nascimento, cpf, role)
VALUES (
    'Admin do Sistema',
    'admin@lodgfy.com',
    '(11) 99999-9999',
    '$2a$10$N9qo8uLOickgx2ZrVzaKe.4rjxiTOGgLwTGk4z4g4Xqc9vY8eG8lO', -- senha: admin123
    '1990-01-01',
    '00000000000',
    'ADMIN'
);

-- Para criar novos usuários admin no futuro, use este template:
-- A senha precisa ser hasheada com BCrypt antes de inserir
/*
INSERT INTO hospedes (nome, email, telefone, senha, data_nascimento, cpf, role)
VALUES (
    'Nome do Admin',
    'email@admin.com',
    'telefone',
    'SENHA_HASH_BCRYPT_AQUI',
    'YYYY-MM-DD',
    'CPF_SEM_FORMATACAO',
    'ADMIN'
);
*/
