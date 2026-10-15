-- =====================================================================
--  Al Toque · Esquema de facturación electrónica SUNAT (UBL 2.1)
--
--  Principios:
--  * 3FN: cada dato vive en un solo lugar. Lo que se puede calcular
--    (monto en letras, nombre de archivo, saldo, intentos de envío,
--    departamento/provincia de un ubigeo) no se guarda.
--  * Excepciones deliberadas, todas inmutables:
--      - el comprobante copia los datos del cliente y del producto
--        (lo emitido ante SUNAT no cambia si luego editas tu catálogo);
--      - el comprobante guarda sus totales tal como se firmaron;
--      - empresa_id se repite en tablas hijas como clave de tenant para
--        filtrar e indexar; su coherencia la garantizan FK compuestas.
--  * Los catálogos SUNAT son tablas: la base rechaza un código inválido
--    antes de que llegue a SUNAT.
--  * Los XML y CDR viven en disco (tabla archivo = metadatos + hash);
--    la base no crece con binarios.
--  * Montos en DECIMAL, nunca FLOAT.
-- =====================================================================

-- ---------------------------------------------------------------------
--  1. Catálogos SUNAT y ubigeo (datos de referencia, solo lectura)
-- ---------------------------------------------------------------------
CREATE TABLE sunat_tipo_comprobante (           -- catálogo 01
    codigo  VARCHAR(2)   NOT NULL PRIMARY KEY,
    nombre  VARCHAR(80)  NOT NULL
);

CREATE TABLE sunat_tipo_documento (             -- catálogo 06
    codigo       VARCHAR(1)   NOT NULL PRIMARY KEY,
    nombre       VARCHAR(80)  NOT NULL,
    abreviatura  VARCHAR(10)  NOT NULL
);

CREATE TABLE sunat_afectacion_igv (             -- catálogo 07
    codigo        VARCHAR(2)   NOT NULL PRIMARY KEY,
    nombre        VARCHAR(80)  NOT NULL,
    tributo       VARCHAR(4)   NOT NULL         -- catálogo 05: 1000 IGV, 9997 EXO, 9998 INA
);

CREATE TABLE sunat_unidad_medida (              -- catálogo 03 (subconjunto de uso común)
    codigo  VARCHAR(3)   NOT NULL PRIMARY KEY,
    nombre  VARCHAR(60)  NOT NULL
);

CREATE TABLE sunat_motivo_nota (                -- catálogos 09 (NC) y 10 (ND)
    tipo_comprobante  VARCHAR(2)   NOT NULL,
    codigo            VARCHAR(2)   NOT NULL,
    nombre            VARCHAR(100) NOT NULL,
    devuelve_todo     BOOLEAN      NOT NULL DEFAULT FALSE,   -- copia todos los ítems del documento
    repone_stock      BOOLEAN      NOT NULL DEFAULT FALSE,
    PRIMARY KEY (tipo_comprobante, codigo),
    CONSTRAINT fk_motivo_tipo FOREIGN KEY (tipo_comprobante) REFERENCES sunat_tipo_comprobante (codigo)
);

CREATE TABLE sunat_bien_detraccion (            -- catálogo 54 (SPOT)
    codigo      VARCHAR(3)    NOT NULL PRIMARY KEY,
    nombre      VARCHAR(150)  NOT NULL,
    porcentaje  DECIMAL(5,2)  NOT NULL,
    CONSTRAINT ck_detraccion_porcentaje CHECK (porcentaje > 0 AND porcentaje <= 100)
);

CREATE TABLE medio_pago (
    codigo        VARCHAR(15)  NOT NULL PRIMARY KEY,   -- EFECTIVO, YAPE, PLIN, ...
    nombre        VARCHAR(60)  NOT NULL,
    codigo_sunat  VARCHAR(3)   NOT NULL,              -- catálogo 59
    es_efectivo   BOOLEAN      NOT NULL DEFAULT FALSE,
    orden         INT          NOT NULL DEFAULT 0
);

-- Ubigeo INEI (el que usa SUNAT), normalizado en tres niveles.
CREATE TABLE departamento (
    codigo  VARCHAR(2)   NOT NULL PRIMARY KEY,
    nombre  VARCHAR(60)  NOT NULL
);

CREATE TABLE provincia (
    codigo               VARCHAR(4)   NOT NULL PRIMARY KEY,
    departamento_codigo  VARCHAR(2)   NOT NULL,
    nombre               VARCHAR(60)  NOT NULL,
    CONSTRAINT fk_provincia_departamento FOREIGN KEY (departamento_codigo) REFERENCES departamento (codigo)
);

CREATE TABLE distrito (
    ubigeo            VARCHAR(6)   NOT NULL PRIMARY KEY,
    provincia_codigo  VARCHAR(4)   NOT NULL,
    nombre            VARCHAR(60)  NOT NULL,
    CONSTRAINT fk_distrito_provincia FOREIGN KEY (provincia_codigo) REFERENCES provincia (codigo)
);
CREATE INDEX ix_distrito_nombre ON distrito (nombre);

-- ---------------------------------------------------------------------
--  2. Planes, empresas (tenants) y su configuración SUNAT
-- ---------------------------------------------------------------------
CREATE TABLE plan (
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo                VARCHAR(20)   NOT NULL,
    nombre                VARCHAR(60)   NOT NULL,
    descripcion           VARCHAR(255),
    precio_mensual        DECIMAL(10,2) NOT NULL,
    precio_anual          DECIMAL(10,2) NOT NULL,
    limite_comprobantes   INT,                       -- NULL = ilimitado
    max_usuarios          INT           NOT NULL,
    max_establecimientos  INT           NOT NULL,
    notas_credito         BOOLEAN       NOT NULL DEFAULT TRUE,
    reportes_avanzados    BOOLEAN       NOT NULL DEFAULT FALSE,
    soporte_prioritario   BOOLEAN       NOT NULL DEFAULT FALSE,
    destacado             BOOLEAN       NOT NULL DEFAULT FALSE,
    orden                 INT           NOT NULL DEFAULT 0,
    activo                BOOLEAN       NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_plan_codigo UNIQUE (codigo),
    CONSTRAINT ck_plan_precios CHECK (precio_mensual >= 0 AND precio_anual >= 0),
    CONSTRAINT ck_plan_limites CHECK (max_usuarios > 0 AND max_establecimientos > 0
                                      AND (limite_comprobantes IS NULL OR limite_comprobantes > 0))
);

CREATE TABLE empresa (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    ruc               VARCHAR(11)   NOT NULL,
    razon_social      VARCHAR(200)  NOT NULL,
    nombre_comercial  VARCHAR(200),
    direccion_fiscal  VARCHAR(250)  NOT NULL,
    ubigeo            VARCHAR(6)    NOT NULL,
    urbanizacion      VARCHAR(100),
    telefono          VARCHAR(20),
    email             VARCHAR(150),
    mensaje_ticket    VARCHAR(160),
    igv_porcentaje    DECIMAL(5,2)  NOT NULL DEFAULT 18.00,   -- 18 general; 10.5/10 restaurantes y hoteles mype (Ley 31556)
    plan_id           BIGINT        NOT NULL,
    estado            VARCHAR(12)   NOT NULL DEFAULT 'PRUEBA',
    prueba_hasta      DATE,
    created_at        DATETIME(6)   NOT NULL,
    updated_at        DATETIME(6)   NOT NULL,
    CONSTRAINT uk_empresa_ruc UNIQUE (ruc),
    CONSTRAINT fk_empresa_plan FOREIGN KEY (plan_id) REFERENCES plan (id),
    CONSTRAINT fk_empresa_ubigeo FOREIGN KEY (ubigeo) REFERENCES distrito (ubigeo),
    CONSTRAINT ck_empresa_ruc CHECK (CHAR_LENGTH(ruc) = 11),
    CONSTRAINT ck_empresa_estado CHECK (estado IN ('PRUEBA', 'ACTIVA', 'SUSPENDIDA')),
    CONSTRAINT ck_empresa_igv CHECK (igv_porcentaje > 0 AND igv_porcentaje <= 18)
);

-- Datos sensibles de conexión con SUNAT, aparte del perfil del negocio (1:1).
CREATE TABLE empresa_sunat (
    empresa_id           BIGINT        NOT NULL PRIMARY KEY,
    ambiente             VARCHAR(12)   NOT NULL DEFAULT 'BETA',
    sol_usuario          VARCHAR(30),
    sol_clave_enc        VARCHAR(255),           -- AES-GCM
    cuenta_detracciones  VARCHAR(20),            -- cuenta del Banco de la Nación (SPOT)
    CONSTRAINT fk_empresa_sunat FOREIGN KEY (empresa_id) REFERENCES empresa (id) ON DELETE CASCADE,
    CONSTRAINT ck_empresa_sunat_ambiente CHECK (ambiente IN ('SIMULADO', 'BETA', 'PRODUCCION'))
);

CREATE TABLE suscripcion (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id       BIGINT        NOT NULL,
    plan_id          BIGINT        NOT NULL,
    periodo          VARCHAR(8)    NOT NULL,     -- MENSUAL | ANUAL
    periodo_inicio   DATE          NOT NULL,
    periodo_fin      DATE          NOT NULL,
    monto            DECIMAL(10,2) NOT NULL,
    estado           VARCHAR(12)   NOT NULL,
    proveedor_pago   VARCHAR(20),
    referencia_pago  VARCHAR(100),
    created_at       DATETIME(6)   NOT NULL,
    CONSTRAINT fk_suscripcion_empresa FOREIGN KEY (empresa_id) REFERENCES empresa (id),
    CONSTRAINT fk_suscripcion_plan    FOREIGN KEY (plan_id)    REFERENCES plan (id),
    CONSTRAINT ck_suscripcion_periodo CHECK (periodo IN ('MENSUAL', 'ANUAL') AND periodo_fin > periodo_inicio),
    CONSTRAINT ck_suscripcion_estado CHECK (estado IN ('PENDIENTE', 'PAGADA', 'VENCIDA', 'CANCELADA'))
);
CREATE INDEX ix_suscripcion_empresa ON suscripcion (empresa_id, periodo_fin);

-- Certificado digital (.pfx) con el que se firma el XML. En BETA se autogenera.
CREATE TABLE certificado_digital (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id    BIGINT        NOT NULL,
    archivo       BLOB          NOT NULL,          -- pocos KB; cifrado por su propia clave
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
--  3. Locales, personas y acceso
-- ---------------------------------------------------------------------
CREATE TABLE establecimiento (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id    BIGINT        NOT NULL,
    codigo_sunat  VARCHAR(4)    NOT NULL,          -- 0000 = domicilio fiscal
    nombre        VARCHAR(120)  NOT NULL,
    direccion     VARCHAR(250),
    ubigeo        VARCHAR(6),
    activo        BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at    DATETIME(6)   NOT NULL,
    CONSTRAINT uk_establecimiento UNIQUE (empresa_id, codigo_sunat),
    CONSTRAINT uk_establecimiento_tenant UNIQUE (id, empresa_id),
    CONSTRAINT fk_establecimiento_empresa FOREIGN KEY (empresa_id) REFERENCES empresa (id),
    CONSTRAINT fk_establecimiento_ubigeo FOREIGN KEY (ubigeo) REFERENCES distrito (ubigeo),
    CONSTRAINT ck_establecimiento_codigo CHECK (CHAR_LENGTH(codigo_sunat) = 4)
);

-- Una persona = un correo. Puede pertenecer a varios negocios (p. ej. el contador).
CREATE TABLE usuario (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre         VARCHAR(120)  NOT NULL,
    email          VARCHAR(150)  NOT NULL,
    password_hash  VARCHAR(100)  NOT NULL,
    activo         BOOLEAN       NOT NULL DEFAULT TRUE,
    ultimo_acceso  DATETIME(6),
    created_at     DATETIME(6)   NOT NULL,
    updated_at     DATETIME(6)   NOT NULL,
    CONSTRAINT uk_usuario_email UNIQUE (email)
);

-- Qué hace cada persona en cada negocio.
CREATE TABLE miembro (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id          BIGINT       NOT NULL,
    empresa_id          BIGINT       NOT NULL,
    establecimiento_id  BIGINT,
    rol                 VARCHAR(15)  NOT NULL,
    activo              BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at          DATETIME(6)  NOT NULL,
    CONSTRAINT uk_miembro UNIQUE (usuario_id, empresa_id),
    CONSTRAINT fk_miembro_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id),
    CONSTRAINT fk_miembro_empresa FOREIGN KEY (empresa_id) REFERENCES empresa (id),
    CONSTRAINT fk_miembro_local FOREIGN KEY (establecimiento_id, empresa_id) REFERENCES establecimiento (id, empresa_id),
    CONSTRAINT ck_miembro_rol CHECK (rol IN ('PROPIETARIO', 'ADMINISTRADOR', 'VENDEDOR'))
);
CREATE INDEX ix_miembro_empresa ON miembro (empresa_id, activo);

-- Serie por tipo: F001 (factura), B001 (boleta), FC01/BC01 (NC), FD01/BD01 (ND).
CREATE TABLE serie (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id          BIGINT       NOT NULL,
    establecimiento_id  BIGINT       NOT NULL,
    tipo_comprobante    VARCHAR(2)   NOT NULL,
    serie               VARCHAR(4)   NOT NULL,
    correlativo_actual  INT          NOT NULL DEFAULT 0,
    activo              BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at          DATETIME(6)  NOT NULL,
    CONSTRAINT uk_serie UNIQUE (empresa_id, tipo_comprobante, serie),
    CONSTRAINT uk_serie_tenant UNIQUE (id, empresa_id),
    CONSTRAINT fk_serie_local FOREIGN KEY (establecimiento_id, empresa_id) REFERENCES establecimiento (id, empresa_id),
    CONSTRAINT fk_serie_tipo FOREIGN KEY (tipo_comprobante) REFERENCES sunat_tipo_comprobante (codigo),
    CONSTRAINT ck_serie_formato CHECK (CHAR_LENGTH(serie) = 4),
    CONSTRAINT ck_serie_correlativo CHECK (correlativo_actual BETWEEN 0 AND 99999999)
);

-- ---------------------------------------------------------------------
--  4. Clientes, productos e inventario
-- ---------------------------------------------------------------------
CREATE TABLE cliente (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id        BIGINT        NOT NULL,
    tipo_documento    VARCHAR(1)    NOT NULL,
    numero_documento  VARCHAR(15)   NOT NULL,
    nombre            VARCHAR(200)  NOT NULL,
    direccion         VARCHAR(250),
    email             VARCHAR(150),
    telefono          VARCHAR(20),
    created_at        DATETIME(6)   NOT NULL,
    updated_at        DATETIME(6)   NOT NULL,
    CONSTRAINT uk_cliente_documento UNIQUE (empresa_id, tipo_documento, numero_documento),
    CONSTRAINT uk_cliente_tenant UNIQUE (id, empresa_id),
    CONSTRAINT fk_cliente_empresa FOREIGN KEY (empresa_id) REFERENCES empresa (id),
    CONSTRAINT fk_cliente_tipo_doc FOREIGN KEY (tipo_documento) REFERENCES sunat_tipo_documento (codigo)
);
CREATE INDEX ix_cliente_nombre ON cliente (empresa_id, nombre);

CREATE TABLE categoria (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id  BIGINT       NOT NULL,
    nombre      VARCHAR(80)  NOT NULL,
    CONSTRAINT uk_categoria UNIQUE (empresa_id, nombre),
    CONSTRAINT uk_categoria_tenant UNIQUE (id, empresa_id),
    CONSTRAINT fk_categoria_empresa FOREIGN KEY (empresa_id) REFERENCES empresa (id)
);

-- precio = precio de venta con IGV (como lo piensa el bodeguero).
-- stock = saldo vigente; su historia completa está en movimiento_inventario.
CREATE TABLE producto (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id       BIGINT         NOT NULL,
    categoria_id     BIGINT,
    codigo           VARCHAR(30)    NOT NULL,
    nombre           VARCHAR(200)   NOT NULL,
    descripcion      VARCHAR(500),
    unidad_medida    VARCHAR(3)     NOT NULL DEFAULT 'NIU',
    precio           DECIMAL(12,2)  NOT NULL,
    tipo_afectacion  VARCHAR(2)     NOT NULL DEFAULT '10',
    aplica_icbper    BOOLEAN        NOT NULL DEFAULT FALSE,
    controla_stock   BOOLEAN        NOT NULL DEFAULT FALSE,
    stock            DECIMAL(12,3)  NOT NULL DEFAULT 0,
    favorito         BOOLEAN        NOT NULL DEFAULT FALSE,
    activo           BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at       DATETIME(6)    NOT NULL,
    updated_at       DATETIME(6)    NOT NULL,
    CONSTRAINT uk_producto_codigo UNIQUE (empresa_id, codigo),
    CONSTRAINT uk_producto_tenant UNIQUE (id, empresa_id),
    CONSTRAINT fk_producto_empresa FOREIGN KEY (empresa_id) REFERENCES empresa (id),
    CONSTRAINT fk_producto_categoria FOREIGN KEY (categoria_id, empresa_id) REFERENCES categoria (id, empresa_id),
    CONSTRAINT fk_producto_unidad FOREIGN KEY (unidad_medida) REFERENCES sunat_unidad_medida (codigo),
    CONSTRAINT fk_producto_afectacion FOREIGN KEY (tipo_afectacion) REFERENCES sunat_afectacion_igv (codigo),
    CONSTRAINT ck_producto_precio CHECK (precio > 0)
);
CREATE INDEX ix_producto_nombre ON producto (empresa_id, nombre);

-- ---------------------------------------------------------------------
--  5. Caja (turno y cuadre)
-- ---------------------------------------------------------------------
CREATE TABLE caja (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id          BIGINT         NOT NULL,
    establecimiento_id  BIGINT         NOT NULL,
    usuario_id          BIGINT         NOT NULL,
    estado              VARCHAR(10)    NOT NULL,
    monto_apertura      DECIMAL(12,2)  NOT NULL DEFAULT 0,
    -- Foto del cuadre al cerrar (no cambia aunque luego se anule algo).
    monto_esperado      DECIMAL(12,2),
    monto_declarado     DECIMAL(12,2),
    notas               VARCHAR(250),
    abierta_at          DATETIME(6)    NOT NULL,
    cerrada_at          DATETIME(6),
    CONSTRAINT uk_caja_tenant UNIQUE (id, empresa_id),
    CONSTRAINT fk_caja_local FOREIGN KEY (establecimiento_id, empresa_id) REFERENCES establecimiento (id, empresa_id),
    CONSTRAINT fk_caja_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id),
    CONSTRAINT ck_caja_estado CHECK (estado IN ('ABIERTA', 'CERRADA')),
    CONSTRAINT ck_caja_cierre CHECK ((estado = 'ABIERTA' AND cerrada_at IS NULL)
                                     OR (estado = 'CERRADA' AND cerrada_at IS NOT NULL AND monto_declarado IS NOT NULL))
);
CREATE INDEX ix_caja_estado ON caja (empresa_id, establecimiento_id, estado);

-- ---------------------------------------------------------------------
--  6. Comprobantes
-- ---------------------------------------------------------------------
-- Tipo, serie y local salen de serie_id: el número legal es (serie_id, correlativo).
CREATE TABLE comprobante (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id         BIGINT         NOT NULL,
    serie_id           BIGINT         NOT NULL,
    correlativo        INT            NOT NULL,
    usuario_id         BIGINT         NOT NULL,
    cliente_id         BIGINT,
    caja_id            BIGINT,
    fecha_emision      DATE           NOT NULL,
    hora_emision       TIME           NOT NULL,
    fecha_vencimiento  DATE,
    moneda             VARCHAR(3)     NOT NULL DEFAULT 'PEN',
    tipo_operacion     VARCHAR(4)     NOT NULL DEFAULT '0101',   -- catálogo 51: 0101 venta interna, 1001 sujeta a detracción
    forma_pago         VARCHAR(10)    NOT NULL DEFAULT 'CONTADO',
    -- Receptor tal como figura en el documento firmado
    cliente_tipo_doc   VARCHAR(1)     NOT NULL,
    cliente_num_doc    VARCHAR(15)    NOT NULL,
    cliente_nombre     VARCHAR(200)   NOT NULL,
    cliente_direccion  VARCHAR(250),
    cliente_email      VARCHAR(150),
    cliente_telefono   VARCHAR(20),
    -- Totales tal como se firmaron
    igv_porcentaje     DECIMAL(5,2)   NOT NULL,
    total_gravado      DECIMAL(12,2)  NOT NULL DEFAULT 0,
    total_exonerado    DECIMAL(12,2)  NOT NULL DEFAULT 0,
    total_inafecto     DECIMAL(12,2)  NOT NULL DEFAULT 0,
    total_igv          DECIMAL(12,2)  NOT NULL DEFAULT 0,
    total_icbper       DECIMAL(12,2)  NOT NULL DEFAULT 0,
    total              DECIMAL(12,2)  NOT NULL,
    observaciones      VARCHAR(500),
    -- Estado
    estado_sunat       VARCHAR(12)    NOT NULL,
    estado_pago        VARCHAR(12)    NOT NULL,
    hash               VARCHAR(100),
    token_publico      VARCHAR(32)    NOT NULL,
    created_at         DATETIME(6)    NOT NULL,
    updated_at         DATETIME(6)    NOT NULL,
    CONSTRAINT uk_comprobante_numero UNIQUE (serie_id, correlativo),
    CONSTRAINT uk_comprobante_token UNIQUE (token_publico),
    CONSTRAINT uk_comprobante_tenant UNIQUE (id, empresa_id),
    CONSTRAINT fk_comprobante_serie FOREIGN KEY (serie_id, empresa_id) REFERENCES serie (id, empresa_id),
    CONSTRAINT fk_comprobante_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id),
    CONSTRAINT fk_comprobante_cliente FOREIGN KEY (cliente_id, empresa_id) REFERENCES cliente (id, empresa_id),
    CONSTRAINT fk_comprobante_caja FOREIGN KEY (caja_id, empresa_id) REFERENCES caja (id, empresa_id),
    CONSTRAINT fk_comprobante_tipo_doc FOREIGN KEY (cliente_tipo_doc) REFERENCES sunat_tipo_documento (codigo),
    CONSTRAINT ck_comprobante_correlativo CHECK (correlativo BETWEEN 1 AND 99999999),
    CONSTRAINT ck_comprobante_moneda CHECK (moneda IN ('PEN', 'USD')),
    CONSTRAINT ck_comprobante_forma_pago CHECK (forma_pago IN ('CONTADO', 'CREDITO')),
    CONSTRAINT ck_comprobante_vencimiento CHECK (fecha_vencimiento IS NULL OR fecha_vencimiento >= fecha_emision),
    CONSTRAINT ck_comprobante_montos CHECK (total_gravado >= 0 AND total_exonerado >= 0 AND total_inafecto >= 0
                                            AND total_igv >= 0 AND total_icbper >= 0 AND total > 0),
    CONSTRAINT ck_comprobante_estado_sunat CHECK (estado_sunat IN
        ('PENDIENTE', 'ACEPTADO', 'OBSERVADO', 'RECHAZADO', 'POR_ANULAR', 'ANULADO')),
    CONSTRAINT ck_comprobante_estado_pago CHECK (estado_pago IN ('PAGADO', 'PARCIAL', 'POR_COBRAR'))
);
CREATE INDEX ix_comprobante_fecha  ON comprobante (empresa_id, fecha_emision);
CREATE INDEX ix_comprobante_estado ON comprobante (empresa_id, estado_sunat);
CREATE INDEX ix_comprobante_pago   ON comprobante (empresa_id, estado_pago);
CREATE INDEX ix_comprobante_doc    ON comprobante (empresa_id, cliente_num_doc);

-- Notas de crédito/débito: documento que modifican y motivo (1:1 con el comprobante nota).
-- comprobante_ref_id es NULL cuando el documento afectado se emitió fuera de Al Toque.
CREATE TABLE comprobante_nota (
    comprobante_id      BIGINT        NOT NULL PRIMARY KEY,
    empresa_id          BIGINT        NOT NULL,
    comprobante_ref_id  BIGINT,
    ref_tipo            VARCHAR(2)    NOT NULL,
    ref_serie           VARCHAR(4)    NOT NULL,
    ref_correlativo     INT           NOT NULL,
    nota_tipo           VARCHAR(2)    NOT NULL,
    motivo_codigo       VARCHAR(2)    NOT NULL,
    sustento            VARCHAR(250)  NOT NULL,
    CONSTRAINT fk_nota_comprobante FOREIGN KEY (comprobante_id, empresa_id) REFERENCES comprobante (id, empresa_id) ON DELETE CASCADE,
    CONSTRAINT fk_nota_ref FOREIGN KEY (comprobante_ref_id, empresa_id) REFERENCES comprobante (id, empresa_id),
    CONSTRAINT fk_nota_ref_tipo FOREIGN KEY (ref_tipo) REFERENCES sunat_tipo_comprobante (codigo),
    CONSTRAINT fk_nota_motivo FOREIGN KEY (nota_tipo, motivo_codigo) REFERENCES sunat_motivo_nota (tipo_comprobante, codigo),
    CONSTRAINT ck_nota_ref_tipo CHECK (ref_tipo IN ('01', '03'))
);
CREATE INDEX ix_nota_ref ON comprobante_nota (comprobante_ref_id);

-- Detracción (SPOT): facturas de más de S/ 700 por bienes o servicios del catálogo 54.
CREATE TABLE comprobante_detraccion (
    comprobante_id  BIGINT         NOT NULL PRIMARY KEY,
    bien_codigo     VARCHAR(3)     NOT NULL,
    porcentaje      DECIMAL(5,2)   NOT NULL,
    monto           DECIMAL(12,2)  NOT NULL,
    cuenta_banco    VARCHAR(20)    NOT NULL,
    medio_pago      VARCHAR(3)     NOT NULL DEFAULT '001',   -- catálogo 59: depósito en cuenta
    CONSTRAINT fk_detraccion_comprobante FOREIGN KEY (comprobante_id) REFERENCES comprobante (id) ON DELETE CASCADE,
    CONSTRAINT fk_detraccion_bien FOREIGN KEY (bien_codigo) REFERENCES sunat_bien_detraccion (codigo),
    CONSTRAINT ck_detraccion_monto CHECK (monto > 0)
);

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
    tipo_afectacion  VARCHAR(2)     NOT NULL,
    valor_venta      DECIMAL(12,2)  NOT NULL,     -- cantidad × valor unitario
    igv              DECIMAL(12,2)  NOT NULL,
    icbper           DECIMAL(12,2)  NOT NULL DEFAULT 0,
    CONSTRAINT uk_detalle_item UNIQUE (comprobante_id, item),
    CONSTRAINT fk_detalle_comprobante FOREIGN KEY (comprobante_id) REFERENCES comprobante (id) ON DELETE CASCADE,
    CONSTRAINT fk_detalle_producto FOREIGN KEY (producto_id) REFERENCES producto (id),
    CONSTRAINT fk_detalle_unidad FOREIGN KEY (unidad_medida) REFERENCES sunat_unidad_medida (codigo),
    CONSTRAINT fk_detalle_afectacion FOREIGN KEY (tipo_afectacion) REFERENCES sunat_afectacion_igv (codigo),
    CONSTRAINT ck_detalle_montos CHECK (cantidad > 0 AND precio_unitario > 0 AND valor_venta >= 0 AND igv >= 0 AND icbper >= 0)
);

-- Cuotas de una factura al crédito (obligatorias desde abril de 2021).
CREATE TABLE comprobante_cuota (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    comprobante_id     BIGINT         NOT NULL,
    numero             INT            NOT NULL,
    monto              DECIMAL(12,2)  NOT NULL,
    fecha_vencimiento  DATE           NOT NULL,
    CONSTRAINT uk_cuota UNIQUE (comprobante_id, numero),
    CONSTRAINT fk_cuota_comprobante FOREIGN KEY (comprobante_id) REFERENCES comprobante (id) ON DELETE CASCADE,
    CONSTRAINT ck_cuota_monto CHECK (monto > 0 AND numero BETWEEN 1 AND 999)
);

-- Cobros: un comprobante puede pagarse en partes y con varios medios. El saldo se calcula.
CREATE TABLE pago (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    comprobante_id  BIGINT         NOT NULL,
    usuario_id      BIGINT         NOT NULL,
    caja_id         BIGINT,
    medio_pago      VARCHAR(15)    NOT NULL,
    monto           DECIMAL(12,2)  NOT NULL,
    referencia      VARCHAR(80),
    pagado_at       DATETIME(6)    NOT NULL,
    CONSTRAINT fk_pago_comprobante FOREIGN KEY (comprobante_id) REFERENCES comprobante (id),
    CONSTRAINT fk_pago_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id),
    CONSTRAINT fk_pago_caja FOREIGN KEY (caja_id) REFERENCES caja (id),
    CONSTRAINT fk_pago_medio FOREIGN KEY (medio_pago) REFERENCES medio_pago (codigo),
    CONSTRAINT ck_pago_monto CHECK (monto > 0)
);
CREATE INDEX ix_pago_comprobante ON pago (comprobante_id);
CREATE INDEX ix_pago_caja ON pago (caja_id);
CREATE INDEX ix_pago_fecha ON pago (pagado_at);

-- Kardex: cada entrada y salida de stock, con su origen.
CREATE TABLE movimiento_inventario (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id        BIGINT         NOT NULL,
    producto_id       BIGINT         NOT NULL,
    tipo              VARCHAR(12)    NOT NULL,     -- VENTA | DEVOLUCION | ANULACION | AJUSTE | INGRESO
    cantidad          DECIMAL(12,3)  NOT NULL,     -- + entra, − sale
    stock_resultante  DECIMAL(12,3)  NOT NULL,
    comprobante_id    BIGINT,
    usuario_id        BIGINT,
    nota              VARCHAR(200),
    created_at        DATETIME(6)    NOT NULL,
    CONSTRAINT fk_movimiento_producto FOREIGN KEY (producto_id, empresa_id) REFERENCES producto (id, empresa_id),
    CONSTRAINT fk_movimiento_comprobante FOREIGN KEY (comprobante_id) REFERENCES comprobante (id),
    CONSTRAINT fk_movimiento_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id),
    CONSTRAINT ck_movimiento_tipo CHECK (tipo IN ('VENTA', 'DEVOLUCION', 'ANULACION', 'AJUSTE', 'INGRESO')),
    CONSTRAINT ck_movimiento_cantidad CHECK (cantidad <> 0)
);
CREATE INDEX ix_movimiento_producto ON movimiento_inventario (producto_id, id);

-- ---------------------------------------------------------------------
--  7. Envíos por ticket: resumen diario (RC) y comunicación de baja (RA)
-- ---------------------------------------------------------------------
CREATE TABLE lote_sunat (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id        BIGINT        NOT NULL,
    tipo              VARCHAR(2)    NOT NULL,     -- RC | RA
    fecha_generacion  DATE          NOT NULL,
    correlativo       INT           NOT NULL,
    fecha_referencia  DATE          NOT NULL,     -- fecha de emisión de los documentos
    estado            VARCHAR(12)   NOT NULL,
    ticket            VARCHAR(40),
    created_at        DATETIME(6)   NOT NULL,
    updated_at        DATETIME(6)   NOT NULL,
    CONSTRAINT uk_lote UNIQUE (empresa_id, tipo, fecha_generacion, correlativo),
    CONSTRAINT fk_lote_empresa FOREIGN KEY (empresa_id) REFERENCES empresa (id),
    CONSTRAINT ck_lote_tipo CHECK (tipo IN ('RC', 'RA')),
    CONSTRAINT ck_lote_estado CHECK (estado IN ('GENERADO', 'ENVIADO', 'ACEPTADO', 'RECHAZADO')),
    CONSTRAINT ck_lote_fechas CHECK (fecha_referencia <= fecha_generacion)
);
CREATE INDEX ix_lote_estado ON lote_sunat (estado);

CREATE TABLE lote_sunat_item (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    lote_id         BIGINT        NOT NULL,
    comprobante_id  BIGINT        NOT NULL,
    linea           INT           NOT NULL,
    condicion       VARCHAR(1),                   -- RC: 1 adicionar, 2 modificar, 3 anular
    motivo          VARCHAR(100)  NOT NULL,
    CONSTRAINT uk_lote_linea UNIQUE (lote_id, linea),
    CONSTRAINT fk_lote_item_lote FOREIGN KEY (lote_id) REFERENCES lote_sunat (id) ON DELETE CASCADE,
    CONSTRAINT fk_lote_item_comprobante FOREIGN KEY (comprobante_id) REFERENCES comprobante (id),
    CONSTRAINT ck_lote_item_condicion CHECK (condicion IS NULL OR condicion IN ('1', '2', '3'))
);
CREATE INDEX ix_lote_item_comprobante ON lote_sunat_item (comprobante_id);

-- ---------------------------------------------------------------------
--  8. Archivos (XML firmado y CDR) y bitácora SUNAT
-- ---------------------------------------------------------------------
-- El contenido vive en disco (ruta relativa); aquí quedan nombre, tamaño y SHA-256.
CREATE TABLE archivo (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id      BIGINT        NOT NULL,
    comprobante_id  BIGINT,
    lote_id         BIGINT,
    tipo            VARCHAR(5)    NOT NULL,     -- XML | CDR
    nombre          VARCHAR(80)   NOT NULL,
    ruta            VARCHAR(255)  NOT NULL,
    bytes           INT           NOT NULL,
    sha256          VARCHAR(64)   NOT NULL,
    created_at      DATETIME(6)   NOT NULL,
    CONSTRAINT uk_archivo_comprobante UNIQUE (comprobante_id, tipo),
    CONSTRAINT uk_archivo_lote UNIQUE (lote_id, tipo),
    CONSTRAINT fk_archivo_empresa FOREIGN KEY (empresa_id) REFERENCES empresa (id),
    CONSTRAINT fk_archivo_comprobante FOREIGN KEY (comprobante_id) REFERENCES comprobante (id) ON DELETE CASCADE,
    CONSTRAINT fk_archivo_lote FOREIGN KEY (lote_id) REFERENCES lote_sunat (id) ON DELETE CASCADE,
    CONSTRAINT ck_archivo_tipo CHECK (tipo IN ('XML', 'CDR')),
    CONSTRAINT ck_archivo_dueno CHECK ((comprobante_id IS NULL) <> (lote_id IS NULL))
);

-- Una fila por cada llamada a SUNAT. El último código/mensaje y los intentos se leen de aquí.
CREATE TABLE envio_sunat (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id      BIGINT         NOT NULL,
    comprobante_id  BIGINT,
    lote_id         BIGINT,
    operacion       VARCHAR(15)    NOT NULL,     -- sendBill | sendSummary | getStatus | getStatusCdr | validacion
    ambiente        VARCHAR(12)    NOT NULL,
    exito           BOOLEAN        NOT NULL,
    codigo          VARCHAR(10),
    mensaje         VARCHAR(1000),
    ticket          VARCHAR(40),
    duracion_ms     INT,
    created_at      DATETIME(6)    NOT NULL,
    CONSTRAINT fk_envio_empresa FOREIGN KEY (empresa_id) REFERENCES empresa (id),
    CONSTRAINT fk_envio_comprobante FOREIGN KEY (comprobante_id) REFERENCES comprobante (id),
    CONSTRAINT fk_envio_lote FOREIGN KEY (lote_id) REFERENCES lote_sunat (id),
    CONSTRAINT ck_envio_dueno CHECK ((comprobante_id IS NULL) <> (lote_id IS NULL))
);
CREATE INDEX ix_envio_comprobante ON envio_sunat (comprobante_id, id);
CREATE INDEX ix_envio_lote ON envio_sunat (lote_id, id);
