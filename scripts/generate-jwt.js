#!/usr/bin/env node
const crypto = require('crypto');

// Configuración
const JWT_SECRET = process.env.JWT_SECRET;
function base64UrlEncode(str) {
    return Buffer.from(str)
        .toString('base64')
        .replace(/\+/g, '-')
        .replace(/\//g, '_')
        .replace(/=/g, '');
}

// HMAC-SHA256
function hmacSha256(data, secret) {
    return crypto.createHmac('sha256', Buffer.from(secret, 'base64')).update(data).digest('base64')
        .replace(/\+/g, '-')
        .replace(/\//g, '_')
        .replace(/=/g, '');
}

function generateJWT(username = 'admin', role = 'ADMIN', expirationHours = 24) {
    const header = {
        "alg": "HS256",
        "typ": "JWT"
    };
    const now = Math.floor(Date.now() / 1000);
    const exp = now + (expirationHours * 3600); 
    const payload = {
        "sub": username,
        "role": role,
        "iat": now,
        "exp": exp
    };
    const encodedHeader = base64UrlEncode(JSON.stringify(header));
    const encodedPayload = base64UrlEncode(JSON.stringify(payload));
    const data = `${encodedHeader}.${encodedPayload}`;
    const signature = hmacSha256(data, JWT_SECRET);
    const jwt = `${encodedHeader}.${encodedPayload}.${signature}`;

    return {
        jwt,
        header,
        payload,
        expiresAt: new Date(exp * 1000).toISOString()
    };
}

function decodeJWT(token) {
    try {
        const parts = token.split('.');
        if (parts.length !== 3) {
            throw new Error('JWT inválido');
        }

        const header = JSON.parse(Buffer.from(parts[0].replace(/-/g, '+').replace(/_/g, '/'), 'base64').toString());
        const payload = JSON.parse(Buffer.from(parts[1].replace(/-/g, '+').replace(/_/g, '/'), 'base64').toString());

        return { header, payload };
    } catch (error) {
        throw new Error('Error decodificando JWT: ' + error.message);
    }
}

function validateJWT(token) {
    try {
        const parts = token.split('.');
        const data = `${parts[0]}.${parts[1]}`;
        const expectedSignature = hmacSha256(data, JWT_SECRET);
        
        const isValid = parts[2] === expectedSignature;
        const decoded = decodeJWT(token);
        const isExpired = decoded.payload.exp < Math.floor(Date.now() / 1000);

        return {
            isValid,
            isExpired,
            decoded
        };
    } catch (error) {
        return {
            isValid: false,
            isExpired: true,
            error: error.message
        };
    }
}

const args = process.argv.slice(2);
const command = args[0];

if (command === 'generate' || !command) {
    const username = args[1] || 'admin@factullama.com';
    const role = args[2] || 'ADMIN';
    const hours = parseInt(args[3]) || 24;

    console.log('FacturaApp');
    console.log('=====================================');
    
    const result = generateJWT(username, role, hours);
    
    console.log(`Usuario: ${username}`);
    console.log(`Rol: ${role}`);
    console.log(`Expira: ${result.expiresAt}`);
    console.log(`Duración: ${hours} horas`);
    console.log('');
    console.log('JWT Token:');
    console.log(result.jwt);
    console.log('');
    console.log('Para usar en Postman/curl:');
    console.log(`Authorization: Bearer ${result.jwt}`);
    
} else if (command === 'decode') {
    const token = args[1];
    if (!token) {
        console.log('Error: Proporciona un token para decodificar');
        console.log('Uso: node generate-jwt.js decode <token>');
        process.exit(1);
    }

    try {
        const decoded = decodeJWT(token);
        console.log('JWT Decodificado:');
        console.log('Header:', JSON.stringify(decoded.header, null, 2));
        console.log('Payload:', JSON.stringify(decoded.payload, null, 2));
    } catch (error) {
        console.log('Error:', error.message);
    }

} else if (command === 'validate') {
    const token = args[1];
    if (!token) {
        console.log('Error: Proporciona un token para validar');
        console.log('Uso: node generate-jwt.js validate <token>');
        process.exit(1);
    }

    const validation = validateJWT(token);
    console.log('Validación de JWT:');
    console.log(`Válido: ${validation.isValid ? '✅' : '❌'}`);
    console.log(`Expirado: ${validation.isExpired ? '❌' : '✅'}`);
    
    if (validation.decoded) {
        console.log(`Usuario: ${validation.decoded.payload.sub}`);
        console.log(`Rol: ${validation.decoded.payload.role}`);
        console.log(`Expira: ${new Date(validation.decoded.payload.exp * 1000).toISOString()}`);
    }

} else if (command === 'help') {
    console.log('Generador de JWT - FacturaApp');
    console.log('=====================================');
    console.log('');
    console.log('Comandos disponibles:');
    console.log('');
    console.log('Generar JWT:');
    console.log('  node generate-jwt.js generate [username] [role] [hours]');
    console.log('  node generate-jwt.js (usa valores por defecto)');
    console.log('');
    console.log('Decodificar JWT:');
    console.log('  node generate-jwt.js decode <token>');
    console.log('');
    console.log('Validar JWT:');
    console.log('  node generate-jwt.js validate <token>');
    console.log('');
    console.log('Ejemplos:');
    console.log('  node generate-jwt.js generate admin ADMIN 24');
    console.log('  node generate-jwt.js generate user USER 1');
    console.log('  node generate-jwt.js decode eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...');

} else {
    console.log('Comando no reconocido. Usa "help" para ver los comandos disponibles.');
}