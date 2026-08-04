-- Desactiva el trial para tu cuenta de desarrollo.
-- Con el interceptor arreglado, si tu trial esta vencido en la BD el sistema
-- te va a redirigir a /registro. Ejecuta esto (reemplaza el email por el tuyo)
-- y vuelve a iniciar sesion.

UPDATE users
SET is_trial = 0,
    trial_expires_at = NULL
WHERE email = 'TU_EMAIL_AQUI';

-- Verificar:
-- SELECT email, is_trial, trial_expires_at FROM users;
