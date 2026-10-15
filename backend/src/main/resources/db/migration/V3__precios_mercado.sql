-- Precios según el estudio de mercado (octubre 2026). Incluyen IGV.
-- Mercado de sistemas con punto de venta: entrada de S/ 39 a S/ 189, promedio S/ 96.
-- Al Toque envía directo a SUNAT (sin OSE/PSE), así que puede costar menos que el promedio y conservar el margen.
-- Pago anual = 10 meses (2 meses gratis).
ALTER TABLE plan ADD COLUMN precio_anual DECIMAL(10,2);

UPDATE plan SET precio_mensual = 39.00, precio_anual = 390.00, limite_comprobantes = 500,
                max_usuarios = 1, max_establecimientos = 1, reportes_avanzados = FALSE, soporte_prioritario = FALSE,
                descripcion = 'Pa'' la bodega y el emprendedor que recién arranca.'
 WHERE codigo = 'CASERITO';

UPDATE plan SET precio_mensual = 69.00, precio_anual = 690.00, limite_comprobantes = 2000,
                max_usuarios = 3, max_establecimientos = 1, reportes_avanzados = TRUE, soporte_prioritario = FALSE,
                descripcion = 'Pa'' el negocio que ya se mueve todos los días.'
 WHERE codigo = 'NEGOCIANTE';

UPDATE plan SET precio_mensual = 139.00, precio_anual = 1390.00, limite_comprobantes = NULL,
                max_usuarios = 15, max_establecimientos = 5, reportes_avanzados = TRUE, soporte_prioritario = TRUE,
                descripcion = 'Pa'' varias sedes, usuarios y mucho movimiento.'
 WHERE codigo = 'EMPRESA';
