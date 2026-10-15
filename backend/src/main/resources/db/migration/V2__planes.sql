-- Planes de la landing (Caserito, Negociante, Empresa). Los precios se editan aquí o desde la BD.
INSERT INTO plan (codigo, nombre, descripcion, precio_mensual, limite_comprobantes, max_usuarios, max_establecimientos,
                  notas_credito, reportes_avanzados, soporte_prioritario, destacado, orden, activo)
VALUES
('CASERITO',   'Caserito',   'Pa'' la bodega y el emprendedor que recién arranca.', 29.00,  300, 1, 1, TRUE, FALSE, FALSE, FALSE, 1, TRUE),
('NEGOCIANTE', 'Negociante', 'Pa'' el negocio que ya se mueve todos los días.',     59.00, 1500, 3, 1, TRUE, TRUE,  FALSE, TRUE,  2, TRUE),
('EMPRESA',    'Empresa',    'Pa'' varias sedes, usuarios y mucho movimiento.',    149.00, NULL, 20, 10, TRUE, TRUE, TRUE,  FALSE, 3, TRUE);
