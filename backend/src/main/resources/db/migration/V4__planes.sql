-- Planes según el estudio de mercado (octubre 2026). Precios con IGV; anual = 10 meses.
INSERT INTO plan (codigo, nombre, descripcion, precio_mensual, precio_anual, limite_comprobantes, max_usuarios,
                  max_establecimientos, notas_credito, reportes_avanzados, soporte_prioritario, destacado, orden, activo)
VALUES
('CASERITO',   'Caserito',   'Pa'' la bodega y el emprendedor que recién arranca.',  39.00,  390.00,  500,  1, 1, TRUE, FALSE, FALSE, FALSE, 1, TRUE),
('NEGOCIANTE', 'Negociante', 'Pa'' el negocio que ya se mueve todos los días.',      69.00,  690.00, 2000,  3, 1, TRUE, TRUE,  FALSE, TRUE,  2, TRUE),
('EMPRESA',    'Empresa',    'Pa'' varias sedes, usuarios y mucho movimiento.',     139.00, 1390.00, NULL, 15, 5, TRUE, TRUE,  TRUE,  FALSE, 3, TRUE);
