-- Catálogos SUNAT (Anexo 8 de la RS 097-2012/SUNAT y modificatorias) y medios de pago.

INSERT INTO sunat_tipo_comprobante (codigo, nombre) VALUES
('01', 'Factura'),
('03', 'Boleta de venta'),
('07', 'Nota de crédito'),
('08', 'Nota de débito');

INSERT INTO sunat_tipo_documento (codigo, nombre, abreviatura) VALUES
('0', 'Sin documento (doc. trib. no domiciliado)', '-'),
('1', 'Documento Nacional de Identidad', 'DNI'),
('4', 'Carné de extranjería', 'CE'),
('6', 'Registro Único de Contribuyentes', 'RUC'),
('7', 'Pasaporte', 'PAS'),
('A', 'Cédula diplomática de identidad', 'CDI');

INSERT INTO sunat_afectacion_igv (codigo, nombre, tributo) VALUES
('10', 'Gravado - Operación onerosa', '1000'),
('20', 'Exonerado - Operación onerosa', '9997'),
('30', 'Inafecto - Operación onerosa', '9998');

INSERT INTO sunat_unidad_medida (codigo, nombre) VALUES
('NIU', 'Unidad'),
('ZZ',  'Servicio'),
('KGM', 'Kilogramo'),
('GRM', 'Gramo'),
('LTR', 'Litro'),
('MLT', 'Mililitro'),
('MTR', 'Metro'),
('MTK', 'Metro cuadrado'),
('MTQ', 'Metro cúbico'),
('GLL', 'Galón'),
('BX',  'Caja'),
('PK',  'Paquete'),
('DZN', 'Docena'),
('BG',  'Bolsa'),
('BO',  'Botella'),
('SA',  'Saco'),
('SET', 'Juego'),
('PR',  'Par'),
('HUR', 'Hora'),
('DAY', 'Día'),
('MON', 'Mes');

-- devuelve_todo: la nota copia todos los ítems; repone_stock: la mercadería regresa al almacén.
INSERT INTO sunat_motivo_nota (tipo_comprobante, codigo, nombre, devuelve_todo, repone_stock) VALUES
('07', '01', 'Anulación de la operación',                TRUE,  TRUE),
('07', '02', 'Anulación por error en el RUC',            TRUE,  TRUE),
('07', '03', 'Corrección por error en la descripción',   FALSE, FALSE),
('07', '04', 'Descuento global',                         FALSE, FALSE),
('07', '05', 'Descuento por ítem',                       FALSE, FALSE),
('07', '06', 'Devolución total',                         TRUE,  TRUE),
('07', '07', 'Devolución por ítem',                      FALSE, TRUE),
('07', '09', 'Disminución en el valor',                  FALSE, FALSE),
('08', '01', 'Intereses por mora',                       FALSE, FALSE),
('08', '02', 'Aumento en el valor',                      FALSE, FALSE),
('08', '03', 'Penalidades / otros conceptos',            FALSE, FALSE);

-- Catálogo 54: bienes y servicios sujetos a detracción (porcentajes vigentes del SPOT).
INSERT INTO sunat_bien_detraccion (codigo, nombre, porcentaje) VALUES
('001', 'Azúcar y melaza de caña', 10.00),
('003', 'Alcohol etílico', 10.00),
('004', 'Recursos hidrobiológicos', 4.00),
('005', 'Maíz amarillo duro', 4.00),
('007', 'Caña de azúcar', 10.00),
('008', 'Madera', 4.00),
('009', 'Arena y piedra', 10.00),
('010', 'Residuos, subproductos, desechos, recortes y desperdicios', 15.00),
('011', 'Bienes gravados con el IGV por renuncia a la exoneración', 10.00),
('012', 'Intermediación laboral y tercerización', 12.00),
('014', 'Carnes y despojos comestibles', 4.00),
('016', 'Aceite de pescado', 10.00),
('017', 'Harina, polvo y pellets de pescado y demás invertebrados acuáticos', 4.00),
('019', 'Arrendamiento de bienes muebles', 10.00),
('020', 'Mantenimiento y reparación de bienes muebles', 12.00),
('021', 'Movimiento de carga', 10.00),
('022', 'Otros servicios empresariales', 12.00),
('023', 'Leche', 4.00),
('024', 'Comisión mercantil', 10.00),
('025', 'Fabricación de bienes por encargo', 10.00),
('026', 'Servicio de transporte de personas', 10.00),
('027', 'Servicio de transporte de bienes', 4.00),
('030', 'Contratos de construcción', 4.00),
('031', 'Oro gravado con el IGV', 10.00),
('032', 'Páprika y otros frutos de los géneros capsicum o pimienta', 10.00),
('034', 'Minerales metálicos no auríferos', 10.00),
('035', 'Bienes exonerados del IGV', 1.50),
('036', 'Oro y demás minerales metálicos exonerados del IGV', 1.50),
('037', 'Demás servicios gravados con el IGV', 12.00),
('039', 'Minerales no metálicos', 10.00),
('040', 'Bien inmueble gravado con IGV', 4.00),
('041', 'Plomo', 15.00);

INSERT INTO medio_pago (codigo, nombre, codigo_sunat, es_efectivo, orden) VALUES
('EFECTIVO',      'Efectivo',               '008', TRUE,  1),
('YAPE',          'Yape',                   '003', FALSE, 2),
('PLIN',          'Plin',                   '003', FALSE, 3),
('TARJETA',       'Tarjeta',                '005', FALSE, 4),
('TRANSFERENCIA', 'Transferencia bancaria', '003', FALSE, 5),
('OTRO',          'Otro',                   '999', FALSE, 6);
