-- Script para crear usuario administrador
-- Ejecutar este script en la base de datos db-factullama

-- Eliminar usuario admin si existe (para evitar duplicados)
DELETE FROM users WHERE email = 'admin@facturacion.com';

-- Crear usuario admin
-- Password: admin (encriptado con BCrypt)
-- $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy = "admin"
INSERT INTO users (id, email, password, nombre, role, activo, created_at, updated_at)
VALUES (
    UNHEX(REPLACE('550e8400-e29b-41d4-a716-446655440000', '-', '')),
    'admin@facturacion.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'Administrador',
    'ADMIN',
    true,
    NOW(),
    NOW()
);

-- Verificar que se creó correctamente
SELECT id, email, nombre, role, activo FROM users WHERE email = 'admin@facturacion.com';
