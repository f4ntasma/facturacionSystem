-- =====================================================================
--  Al Toque · Esquema de facturación electrónica (SUNAT, UBL 2.1)
--  Boletas (03), facturas (01), notas de crédito (07) y débito (08),
--  resúmenes diarios (RC) y comunicaciones de baja (RA).
--  Multiempresa: cada fila de negocio cuelga de una empresa (tenant).
--  Montos en DECIMAL; nunca FLOAT.
-- =====================================================================

-- ---------------------------------------------------------------------
--  Planes y suscripciones
-- ---------------------------------------------------------------------
CREATE TABLE plan (
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo                VARCHAR(20)   NOT NULL,
    nombre                VARCHAR(60)   NOT NULL,
    descripcion           VARCHAR(255),
    precio_mensual        DECIMAL(10,2) NOT NULL,
    limite_comprobantes   INT,                       -- NULL = ilimitado
    max_usuarios          INT           NOT NULL,
    max_establecimientos  INT           NOT NULL,
    notas_credito         BOOLEAN       NOT NULL DEFAULT TRUE,
    reportes_avanzados    BOOLEAN       NOT NULL DEFAULT FALSE,
    soporte_prioritario   BOOLEAN       NOT NULL DEFAULT FALSE,
    destacado             BOOLEAN       NOT NULL DEFAULT FALSE,
    orden                 INT           NOT NULL DEFAULT 0,
    activo                BOOLEAN       NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_plan_codigo UNIQUE (codigo)
);

CREATE TABLE empresa (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    ruc               VARCHAR(11)      NOT NULL,
    razon_social      VARCHAR(200)  NOT NULL,
    nombre_comercial  VARCHAR(200),
    direccion_fiscal  VARCHAR(250)  NOT NULL,
    ubigeo            VARCHAR(6)       NOT NULL DEFAULT '150101',
    departamento      VARCHAR(60)   NOT NULL DEFAULT 'LIMA',
    provincia         VARCHAR(60)   NOT NULL DEFAULT 'LIMA',
    distrito          VARCHAR(60)   NOT NULL DEFAULT 'LIMA',
    urbanizacion      VARCHAR(100),
    telefono          VARCHAR(20),
    email             VARCHAR(150),
    mensaje_ticket    VARCHAR(160),
    igv_porcentaje    DECIMAL(5,2)  NOT NULL DEFAULT 18.00,
    ambiente_sunat    VARCHAR(12)   NOT NULL DEFAULT 'SIMULADO',  -- SIMULADO | BETA | PRODUCCION
    sol_usuario       VARCHAR(30),
    sol_clave_enc     VARCHAR(255),                               -- cifrada (AES-GCM)
    plan_id           BIGINT        NOT NULL,
    estado            VARCHAR(12)   NOT NULL DEFAULT 'PRUEBA',    -- PRUEBA | ACTIVA | SUSPENDIDA
    prueba_hasta      DATE,
    created_at        DATETIME(6)   NOT NULL,
    updated_at        DATETIME(6)   NOT NULL,
    CONSTRAINT uk_empresa_ruc UNIQUE (ruc),
    CONSTRAINT fk_empresa_plan FOREIGN KEY (plan_id) REFERENCES plan (id)
);

CREATE TABLE suscripcion (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id       BIGINT        NOT NULL,
    plan_id          BIGINT        NOT NULL,
    periodo_inicio   DATE          NOT NULL,
    periodo_fin      DATE          NOT NULL,
    monto            DECIMAL(10,2) NOT NULL,
    estado           VARCHAR(12)   NOT NULL,                      -- PENDIENTE | PAGADA | VENCIDA | CANCELADA
    proveedor_pago   VARCHAR(20),
    referencia_pago  VARCHAR(100),
    created_at       DATETIME(6)   NOT NULL,
    CONSTRAINT fk_suscripcion_empresa FOREIGN KEY (empresa_id) REFERENCES empresa (id),
    CONSTRAINT fk_suscripcion_plan    FOREIGN KEY (plan_id)    REFERENCES plan (id)
);
CREATE INDEX ix_suscripcion_empresa ON suscripcion (empresa_id, periodo_fin);

-- Certificado digital (.pfx) con el que se firma el XML. En BETA se autogenera.
CREATE TABLE certificado_digital (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id    BIGINT        NOT NULL,
    archivo       LONGBLOB      NOT NULL,
    clave_enc     VARCHAR(255)  NOT NULL,
    sujeto        VARCHAR(300),
    emisor        VARCHAR(300),
    numero_serie  VARCHAR(80),
    valido_desde  DATETIME(6),
    valido_hasta  DATETIME(6),
    autogenerado  BOOLEAN       NOT NULL DEFAULT FALSE,
    activo        BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at    DATETIME(6)   NOT NULL,
    CONSTRAINT fk_certificado_empresa FOREIGN KEY (empresa_id) REFERENCES empresa (id)
);
CREATE INDEX ix_certificado_empresa ON certificado_digital (empresa_id, activo);

-- ---------------------------------------------------------------------
--  Locales, usuarios y series
-- ---------------------------------------------------------------------
CREATE TABLE establecimiento (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id    BIGINT        NOT NULL,
    codigo_sunat  VARCHAR(4)       NOT NULL,                          -- 0000 = domicilio fiscal
    nombre        VARCHAR(120)  NOT NULL,
    direccion     VARCHAR(250),
    ubigeo        VARCHAR(6),
    activo        BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at    DATETIME(6)   NOT NULL,
    CONSTRAINT uk_establecimiento UNIQUE (empresa_id, codigo_sunat),
    CONSTRAINT fk_establecimiento_empresa FOREIGN KEY (empresa_id) REFERENCES empresa (id)
);

CREATE TABLE usuario (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id          BIGINT        NOT NULL,
    establecimiento_id  BIGINT,
    nombre              VARCHAR(120)  NOT NULL,
    email               VARCHAR(150)  NOT NULL,
    password_hash       VARCHAR(100)  NOT NULL,
    rol                 VARCHAR(15)   NOT NULL,                    -- PROPIETARIO | ADMINISTRADOR | VENDEDOR
    activo              BOOLEAN       NOT NULL DEFAULT TRUE,
    ultimo_acceso       DATETIME(6),
    created_at          DATETIME(6)   NOT NULL,
    updated_at          DATETIME(6)   NOT NULL,
    CONSTRAINT uk_usuario_email UNIQUE (email),
    CONSTRAINT fk_usuario_empresa FOREIGN KEY (empresa_id) REFERENCES empresa (id),
    CONSTRAINT fk_usuario_establecimiento FOREIGN KEY (establecimiento_id) REFERENCES establecimiento (id)
);

-- Serie por tipo de comprobante: F001 (factura), B001 (boleta), FC01/BC01 (NC), FD01/BD01 (ND)
CREATE TABLE serie (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id          BIGINT      NOT NULL,
    establecimiento_id  BIGINT      NOT NULL,
    tipo_comprobante    VARCHAR(2)     NOT NULL,
    serie               VARCHAR(4)     NOT NULL,
    correlativo_actual  INT         NOT NULL DEFAULT 0,
    activo              BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at          DATETIME(6) NOT NULL,
    CONSTRAINT uk_serie UNIQUE (empresa_id, tipo_comprobante, serie),
    CONSTRAINT fk_serie_empresa FOREIGN KEY (empresa_id) REFERENCES empresa (id),
    CONSTRAINT fk_serie_establecimiento FOREIGN KEY (establecimiento_id) REFERENCES establecimiento (id)
);

-- ---------------------------------------------------------------------
--  Catálogo: clientes y productos
-- ---------------------------------------------------------------------
-- tipo_documento (catálogo 06): 0 sin doc, 1 DNI, 4 CE, 6 RUC, 7 pasaporte
CREATE TABLE cliente (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id        BIGINT        NOT NULL,
    tipo_documento    VARCHAR(1)       NOT NULL,
    numero_documento  VARCHAR(15)   NOT NULL,
    nombre            VARCHAR(200)  NOT NULL,
    direccion         VARCHAR(250),
    email             VARCHAR(150),
    telefono          VARCHAR(20),
    created_at        DATETIME(6)   NOT NULL,
    updated_at        DATETIME(6)   NOT NULL,
    CONSTRAINT uk_cliente_documento UNIQUE (empresa_id, tipo_documento, numero_documento),
    CONSTRAINT fk_cliente_empresa FOREIGN KEY (empresa_id) REFERENCES empresa (id)
);
CREATE INDEX ix_cliente_nombre ON cliente (empresa_id, nombre);

CREATE TABLE categoria (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id  BIGINT       NOT NULL,
    nombre      VARCHAR(80)  NOT NULL,
    CONSTRAINT uk_categoria UNIQUE (empresa_id, nombre),
    CONSTRAINT fk_categoria_empresa FOREIGN KEY (empresa_id) REFERENCES empresa (id)
);

-- precio = precio de venta con IGV (como lo piensa el bodeguero).
-- tipo_afectacion (catálogo 07): 10 gravado, 20 exonerado, 30 inafecto
CREATE TABLE producto (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id       BIGINT         NOT NULL,
    categoria_id     BIGINT,
    codigo           VARCHAR(30)    NOT NULL,
    nombre           VARCHAR(200)   NOT NULL,
    descripcion      VARCHAR(500),
    unidad_medida    VARCHAR(3)     NOT NULL DEFAULT 'NIU',          -- catálogo 03: NIU, ZZ, KGM, LTR...
    precio           DECIMAL(12,2)  NOT NULL,
    tipo_afectacion  VARCHAR(2)        NOT NULL DEFAULT '10',
    aplica_icbper    BOOLEAN        NOT NULL DEFAULT FALSE,          -- bolsas plásticas
    controla_stock   BOOLEAN        NOT NULL DEFAULT FALSE,
    stock            DECIMAL(12,3)  NOT NULL DEFAULT 0,
    favorito         BOOLEAN        NOT NULL DEFAULT FALSE,          -- botón rápido en "Emitir"
    activo           BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at       DATETIME(6)    NOT NULL,
    updated_at       DATETIME(6)    NOT NULL,
    CONSTRAINT uk_producto_codigo UNIQUE (empresa_id, codigo),
    CONSTRAINT fk_producto_empresa FOREIGN KEY (empresa_id) REFERENCES empresa (id),
    CONSTRAINT fk_producto_categoria FOREIGN KEY (categoria_id) REFERENCES categoria (id)
);
CREATE INDEX ix_producto_nombre ON producto (empresa_id, nombre);

-- ---------------------------------------------------------------------
--  Caja (cuadre del día)
-- ---------------------------------------------------------------------
CREATE TABLE caja (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id          BIGINT         NOT NULL,
    establecimiento_id  BIGINT         NOT NULL,
    usuario_id          BIGINT         NOT NULL,
    estado              VARCHAR(10)    NOT NULL,                 -- ABIERTA | CERRADA
    monto_apertura      DECIMAL(12,2)  NOT NULL DEFAULT 0,
    monto_esperado      DECIMAL(12,2),
    monto_declarado     DECIMAL(12,2),
    diferencia          DECIMAL(12,2),
    notas               VARCHAR(250),
    abierta_at          DATETIME(6)    NOT NULL,
    cerrada_at          DATETIME(6),
    CONSTRAINT fk_caja_empresa FOREIGN KEY (empresa_id) REFERENCES empresa (id),
    CONSTRAINT fk_caja_establecimiento FOREIGN KEY (establecimiento_id) REFERENCES establecimiento (id),
    CONSTRAINT fk_caja_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id)
);
CREATE INDEX ix_caja_estado ON caja (empresa_id, establecimiento_id, estado);

-- ---------------------------------------------------------------------
--  Comprobantes
-- ---------------------------------------------------------------------
-- Los datos del cliente y del producto se copian (snapshot): el comprobante
-- emitido no cambia aunque luego se edite el cliente o el producto.
CREATE TABLE comprobante (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id           BIGINT         NOT NULL,
    establecimiento_id   BIGINT         NOT NULL,
    serie_id             BIGINT         NOT NULL,
    usuario_id           BIGINT         NOT NULL,
    cliente_id           BIGINT,
    caja_id              BIGINT,
    tipo_comprobante     VARCHAR(2)        NOT NULL,               -- 01 | 03 | 07 | 08
    serie                VARCHAR(4)        NOT NULL,
    correlativo          INT            NOT NULL,
    fecha_emision        DATE           NOT NULL,
    hora_emision         TIME           NOT NULL,
    fecha_vencimiento    DATE,
    moneda               VARCHAR(3)        NOT NULL DEFAULT 'PEN',
    tipo_operacion       VARCHAR(4)        NOT NULL DEFAULT '0101',
    forma_pago           VARCHAR(10)    NOT NULL DEFAULT 'CONTADO', -- CONTADO | CREDITO
    cliente_tipo_doc     VARCHAR(1)        NOT NULL,
    cliente_num_doc      VARCHAR(15)    NOT NULL,
    cliente_nombre       VARCHAR(200)   NOT NULL,
    cliente_direccion    VARCHAR(250),
    cliente_email        VARCHAR(150),
    cliente_telefono     VARCHAR(20),
    igv_porcentaje       DECIMAL(5,2)   NOT NULL,
    total_gravado        DECIMAL(12,2)  NOT NULL DEFAULT 0,
    total_exonerado      DECIMAL(12,2)  NOT NULL DEFAULT 0,
    total_inafecto       DECIMAL(12,2)  NOT NULL DEFAULT 0,
    total_igv            DECIMAL(12,2)  NOT NULL DEFAULT 0,
    total_icbper         DECIMAL(12,2)  NOT NULL DEFAULT 0,
    total                DECIMAL(12,2)  NOT NULL,
    total_letras         VARCHAR(255)   NOT NULL,
    observaciones        VARCHAR(500),
    -- Notas de crédito / débito: documento afectado y motivo (catálogos 09 y 10)
    comprobante_ref_id   BIGINT,
    ref_tipo             VARCHAR(2),
    ref_serie            VARCHAR(4),
    ref_correlativo      INT,
    motivo_codigo        VARCHAR(2),
    motivo_descripcion   VARCHAR(250),
    -- SUNAT
    estado_sunat         VARCHAR(12)    NOT NULL,               -- PENDIENTE | ACEPTADO | OBSERVADO | RECHAZADO | POR_ANULAR | ANULADO
    sunat_codigo         VARCHAR(10),
    sunat_mensaje        VARCHAR(1000),
    sunat_intentos       INT            NOT NULL DEFAULT 0,
    enviado_at           DATETIME(6),
    hash                 VARCHAR(100),
    nombre_archivo       VARCHAR(60)    NOT NULL,
    -- Cobranza
    estado_pago          VARCHAR(12)    NOT NULL,               -- PAGADO | PARCIAL | POR_COBRAR
    monto_pagado         DECIMAL(12,2)  NOT NULL DEFAULT 0,
    -- Anulación
    anulacion_motivo     VARCHAR(250),
    anulado_at           DATETIME(6),
    token_publico        VARCHAR(32)    NOT NULL,               -- enlace para WhatsApp / QR
    created_at           DATETIME(6)    NOT NULL,
    updated_at           DATETIME(6)    NOT NULL,
    CONSTRAINT uk_comprobante_numero UNIQUE (empresa_id, tipo_comprobante, serie, correlativo),
    CONSTRAINT uk_comprobante_token UNIQUE (token_publico),
    CONSTRAINT fk_comprobante_empresa FOREIGN KEY (empresa_id) REFERENCES empresa (id),
    CONSTRAINT fk_comprobante_establecimiento FOREIGN KEY (establecimiento_id) REFERENCES establecimiento (id),
    CONSTRAINT fk_comprobante_serie FOREIGN KEY (serie_id) REFERENCES serie (id),
    CONSTRAINT fk_comprobante_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id),
    CONSTRAINT fk_comprobante_cliente FOREIGN KEY (cliente_id) REFERENCES cliente (id),
    CONSTRAINT fk_comprobante_caja FOREIGN KEY (caja_id) REFERENCES caja (id),
    CONSTRAINT fk_comprobante_ref FOREIGN KEY (comprobante_ref_id) REFERENCES comprobante (id)
);
CREATE INDEX ix_comprobante_fecha  ON comprobante (empresa_id, fecha_emision);
CREATE INDEX ix_comprobante_estado ON comprobante (empresa_id, estado_sunat);
CREATE INDEX ix_comprobante_pago   ON comprobante (empresa_id, estado_pago);
CREATE INDEX ix_comprobante_doc    ON comprobante (empresa_id, cliente_num_doc);

CREATE TABLE comprobante_detalle (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    comprobante_id   BIGINT         NOT NULL,
    producto_id      BIGINT,
    item             INT            NOT NULL,
    codigo           VARCHAR(30),
    descripcion      VARCHAR(500)   NOT NULL,
    unidad_medida    VARCHAR(3)     NOT NULL,
    cantidad         DECIMAL(12,3)  NOT NULL,
    precio_unitario  DECIMAL(14,4)  NOT NULL,     -- con IGV
    valor_unitario   DECIMAL(18,10) NOT NULL,     -- sin IGV
    tipo_afectacion  VARCHAR(2)        NOT NULL,
    valor_venta      DECIMAL(12,2)  NOT NULL,     -- cantidad × valor unitario
    igv              DECIMAL(12,2)  NOT NULL,
    icbper           DECIMAL(12,2)  NOT NULL DEFAULT 0,
    total            DECIMAL(12,2)  NOT NULL,
    CONSTRAINT uk_detalle_item UNIQUE (comprobante_id, item),
    CONSTRAINT fk_detalle_comprobante FOREIGN KEY (comprobante_id) REFERENCES comprobante (id) ON DELETE CASCADE,
    CONSTRAINT fk_detalle_producto FOREIGN KEY (producto_id) REFERENCES producto (id)
);

-- Cuotas de una factura al crédito (obligatorias para SUNAT desde 2021)
CREATE TABLE comprobante_cuota (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    comprobante_id     BIGINT         NOT NULL,
    numero             INT            NOT NULL,
    monto              DECIMAL(12,2)  NOT NULL,
    fecha_vencimiento  DATE           NOT NULL,
    CONSTRAINT uk_cuota UNIQUE (comprobante_id, numero),
    CONSTRAINT fk_cuota_comprobante FOREIGN KEY (comprobante_id) REFERENCES comprobante (id) ON DELETE CASCADE
);

-- Cobros: un comprobante puede pagarse en partes y con varios medios
CREATE TABLE pago (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    comprobante_id  BIGINT         NOT NULL,
    usuario_id      BIGINT         NOT NULL,
    caja_id         BIGINT,
    metodo          VARCHAR(15)    NOT NULL,      -- EFECTIVO | YAPE | PLIN | TARJETA | TRANSFERENCIA | OTRO
    monto           DECIMAL(12,2)  NOT NULL,
    referencia      VARCHAR(80),
    pagado_at       DATETIME(6)    NOT NULL,
    CONSTRAINT fk_pago_comprobante FOREIGN KEY (comprobante_id) REFERENCES comprobante (id),
    CONSTRAINT fk_pago_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id),
    CONSTRAINT fk_pago_caja FOREIGN KEY (caja_id) REFERENCES caja (id)
);
CREATE INDEX ix_pago_fecha ON pago (pagado_at);

-- XML firmado y CDR (constancia de recepción) de cada comprobante
CREATE TABLE comprobante_archivo (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    comprobante_id  BIGINT        NOT NULL,
    tipo            VARCHAR(5)    NOT NULL,       -- XML | CDR
    nombre          VARCHAR(80)   NOT NULL,
    contenido       LONGBLOB      NOT NULL,
    created_at      DATETIME(6)   NOT NULL,
    CONSTRAINT uk_archivo UNIQUE (comprobante_id, tipo),
    CONSTRAINT fk_archivo_comprobante FOREIGN KEY (comprobante_id) REFERENCES comprobante (id) ON DELETE CASCADE
);

-- ---------------------------------------------------------------------
--  Resumen diario (RC): anulación de boletas y sus notas
-- ---------------------------------------------------------------------
CREATE TABLE resumen_diario (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id        BIGINT        NOT NULL,
    identificador     VARCHAR(20)   NOT NULL,     -- RC-AAAAMMDD-n
    fecha_referencia  DATE          NOT NULL,
    fecha_generacion  DATE          NOT NULL,
    correlativo       INT           NOT NULL,
    estado            VARCHAR(12)   NOT NULL,     -- GENERADO | ENVIADO | ACEPTADO | RECHAZADO
    ticket            VARCHAR(40),
    sunat_codigo      VARCHAR(10),
    sunat_mensaje     VARCHAR(1000),
    xml               LONGBLOB,
    cdr               LONGBLOB,
    created_at        DATETIME(6)   NOT NULL,
    updated_at        DATETIME(6)   NOT NULL,
    CONSTRAINT uk_resumen UNIQUE (empresa_id, identificador),
    CONSTRAINT fk_resumen_empresa FOREIGN KEY (empresa_id) REFERENCES empresa (id)
);

CREATE TABLE resumen_diario_item (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    resumen_id      BIGINT   NOT NULL,
    comprobante_id  BIGINT   NOT NULL,
    linea           INT      NOT NULL,
    condicion       VARCHAR(1)  NOT NULL,            -- 1 adicionar | 2 modificar | 3 anular
    CONSTRAINT fk_resumen_item_resumen FOREIGN KEY (resumen_id) REFERENCES resumen_diario (id) ON DELETE CASCADE,
    CONSTRAINT fk_resumen_item_comprobante FOREIGN KEY (comprobante_id) REFERENCES comprobante (id)
);

-- ---------------------------------------------------------------------
--  Comunicación de baja (RA): anulación de facturas y sus notas
-- ---------------------------------------------------------------------
CREATE TABLE comunicacion_baja (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id        BIGINT        NOT NULL,
    identificador     VARCHAR(20)   NOT NULL,     -- RA-AAAAMMDD-n
    fecha_referencia  DATE          NOT NULL,
    fecha_generacion  DATE          NOT NULL,
    correlativo       INT           NOT NULL,
    estado            VARCHAR(12)   NOT NULL,
    ticket            VARCHAR(40),
    sunat_codigo      VARCHAR(10),
    sunat_mensaje     VARCHAR(1000),
    xml               LONGBLOB,
    cdr               LONGBLOB,
    created_at        DATETIME(6)   NOT NULL,
    updated_at        DATETIME(6)   NOT NULL,
    CONSTRAINT uk_baja UNIQUE (empresa_id, identificador),
    CONSTRAINT fk_baja_empresa FOREIGN KEY (empresa_id) REFERENCES empresa (id)
);

CREATE TABLE comunicacion_baja_item (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    baja_id         BIGINT        NOT NULL,
    comprobante_id  BIGINT        NOT NULL,
    linea           INT           NOT NULL,
    motivo          VARCHAR(100)  NOT NULL,
    CONSTRAINT fk_baja_item_baja FOREIGN KEY (baja_id) REFERENCES comunicacion_baja (id) ON DELETE CASCADE,
    CONSTRAINT fk_baja_item_comprobante FOREIGN KEY (comprobante_id) REFERENCES comprobante (id)
);

-- ---------------------------------------------------------------------
--  Bitácora de envíos a SUNAT (cada intento queda registrado)
-- ---------------------------------------------------------------------
CREATE TABLE envio_sunat (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id      BIGINT         NOT NULL,
    comprobante_id  BIGINT,
    resumen_id      BIGINT,
    baja_id         BIGINT,
    operacion       VARCHAR(15)    NOT NULL,      -- sendBill | sendSummary | getStatus
    ambiente        VARCHAR(12)    NOT NULL,
    exito           BOOLEAN        NOT NULL,
    codigo          VARCHAR(10),
    mensaje         VARCHAR(1000),
    ticket          VARCHAR(40),
    duracion_ms     INT,
    created_at      DATETIME(6)    NOT NULL,
    CONSTRAINT fk_envio_empresa FOREIGN KEY (empresa_id) REFERENCES empresa (id),
    CONSTRAINT fk_envio_comprobante FOREIGN KEY (comprobante_id) REFERENCES comprobante (id),
    CONSTRAINT fk_envio_resumen FOREIGN KEY (resumen_id) REFERENCES resumen_diario (id),
    CONSTRAINT fk_envio_baja FOREIGN KEY (baja_id) REFERENCES comunicacion_baja (id)
);
CREATE INDEX ix_envio_comprobante ON envio_sunat (comprobante_id);
