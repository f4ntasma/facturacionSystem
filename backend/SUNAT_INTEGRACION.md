# Integración de Facturación Electrónica SUNAT

## 📋 Descripción

Este módulo implementa la integración completa con SUNAT para la emisión de comprobantes electrónicos (Facturas y Boletas) utilizando el estándar UBL 2.1 y las librerías de Project OpenUBL.

## 🔧 Tecnologías Utilizadas

- **Java 17** con **Spring Boot 3.2**
- **Project OpenUBL XBuilder** - Construcción de documentos UBL 2.1
- **Project OpenUBL XSender** - Envío a servicios web de SUNAT
- **Jakarta XML Binding (JAXB)** - Manejo de XML
- **Apache XML Security** - Firma digital de documentos
- **BouncyCastle** - Manejo de certificados digitales

## 📦 Dependencias Agregadas

```xml
<!-- XBuilder - Construcción de documentos UBL 2.1 -->
<dependency>
    <groupId>io.github.project-openubl</groupId>
    <artifactId>xbuilder</artifactId>
    <version>2.0.0.Final</version>
</dependency>

<!-- XSender - Envío a SUNAT -->
<dependency>
    <groupId>io.github.project-openubl</groupId>
    <artifactId>xsender</artifactId>
    <version>2.0.0.Final</version>
</dependency>

<!-- Jakarta XML Binding API (Java 17) -->
<dependency>
    <groupId>jakarta.xml.bind</groupId>
    <artifactId>jakarta.xml.bind-api</artifactId>
    <version>4.0.0</version>
</dependency>

<!-- JAXB Runtime -->
<dependency>
    <groupId>org.glassfish.jaxb</groupId>
    <artifactId>jaxb-runtime</artifactId>
    <version>4.0.2</version>
</dependency>

<!-- Apache XML Security -->
<dependency>
    <groupId>org.apache.santuario</groupId>
    <artifactId>xmlsec</artifactId>
    <version>3.0.2</version>
</dependency>

<!-- BouncyCastle -->
<dependency>
    <groupId>org.bouncycastle</groupId>
    <artifactId>bcprov-jdk18on</artifactId>
    <version>1.77</version>
</dependency>
```

## 🏗️ Estructura del Módulo

```
com.facturacion.sunat/
├── config/
│   └── SunatConfig.java              # Configuración de SUNAT
├── controller/
│   └── SunatController.java          # Endpoints REST
├── dto/
│   ├── DocumentoSunatRequest.java    # Request para emisión
│   └── DocumentoSunatResponse.java   # Response de SUNAT
├── exception/
│   ├── SunatException.java           # Excepción de SUNAT
│   ├── FirmaDigitalException.java    # Excepción de firma
│   └── SunatExceptionHandler.java    # Manejador global
└── service/
    ├── FirmadorService.java          # Servicio de firma digital
    └── SunatService.java             # Servicio principal
```

## ⚙️ Configuración

### 1. Certificado Digital

Coloca tu certificado digital `.pfx` en:
```
backend/src/main/resources/certificado/certificado.pfx
```

### 2. Variables de Configuración

Edita `application.properties`:

```properties
# Credenciales SUNAT
sunat.ruc=20000000001
sunat.usuario=MODDATOS
sunat.clave=MODDATOS

# Certificado
sunat.certificado-path=classpath:certificado/certificado.pfx
sunat.certificado-password=tu_password

# Datos de la empresa
sunat.razon-social=TU EMPRESA S.A.C.
sunat.nombre-comercial=TU EMPRESA
sunat.direccion=TU DIRECCION
sunat.ubigeo=150101
```

### 3. Ambiente de Pruebas vs Producción

**BETA (Pruebas):**
```properties
sunat.produccion=false
sunat.url-factura=https://e-beta.sunat.gob.pe/ol-ti-itcpfegem-beta/billService
```

**PRODUCCIÓN:**
```properties
sunat.produccion=true
sunat.url-factura=https://e-factura.sunat.gob.pe/ol-ti-itcpfegem/billService
```

## 🚀 Uso de la API

### Emitir Factura

```bash
POST /api/sunat/factura
Authorization: Bearer {token}
Content-Type: application/json

{
  "tipoDocumento": "01",
  "serie": "F001",
  "numero": 1,
  "fechaEmision": "2024-02-08",
  "tipoMoneda": "PEN",
  "cliente": {
    "tipoDocumento": "6",
    "numeroDocumento": "20123456789",
    "razonSocial": "CLIENTE S.A.C.",
    "direccion": "AV. EJEMPLO 456",
    "email": "cliente@ejemplo.com"
  },
  "items": [
    {
      "cantidad": 10,
      "unidadMedida": "NIU",
      "descripcion": "Producto de ejemplo",
      "valorUnitario": 100.00,
      "precioUnitario": 118.00,
      "codigoProducto": "PROD001",
      "tipoIgv": "10",
      "igv": 18.00,
      "valorVenta": 1000.00
    }
  ],
  "totalGravadas": 1000.00,
  "totalIgv": 180.00,
  "importeTotal": 1180.00,
  "observaciones": "Observaciones adicionales"
}
```

### Emitir Boleta

```bash
POST /api/sunat/boleta
Authorization: Bearer {token}
Content-Type: application/json

{
  "tipoDocumento": "03",
  "serie": "B001",
  "numero": 1,
  "fechaEmision": "2024-02-08",
  "cliente": {
    "tipoDocumento": "1",
    "numeroDocumento": "12345678",
    "razonSocial": "JUAN PEREZ",
    "direccion": "CALLE EJEMPLO 789"
  },
  "items": [...],
  "importeTotal": 118.00
}
```

### Consultar Estado

```bash
GET /api/sunat/consultar/01/F001/1
Authorization: Bearer {token}
```

### Verificar Certificado

```bash
GET /api/sunat/certificado/info
Authorization: Bearer {token}
```

## 📝 Respuesta de SUNAT

```json
{
  "exitoso": true,
  "mensaje": "Documento aceptado por SUNAT",
  "codigoRespuesta": "0",
  "descripcionRespuesta": "La Factura numero F001-1, ha sido aceptada",
  "tipoDocumento": "01",
  "serie": "F001",
  "numero": 1,
  "nombreArchivo": "20000000001-01-F001-1",
  "xmlBase64": "PD94bWwgdmVyc2lvbj0iMS4wIi...",
  "xmlNombreArchivo": "20000000001-01-F001-1.xml",
  "cdrBase64": "UEsDBBQACAgIAA...",
  "cdrNombreArchivo": "R-20000000001-01-F001-1.xml"
}
```

## 🔐 Seguridad

- Todos los endpoints requieren autenticación JWT
- Solo usuarios con rol `ADMIN` pueden emitir documentos
- El certificado digital se carga de forma segura al iniciar la aplicación
- Las credenciales de SUNAT se configuran mediante variables de entorno

## ⚠️ Manejo de Errores

El sistema maneja los siguientes tipos de errores:

1. **SunatException**: Errores de comunicación con SUNAT
2. **FirmaDigitalException**: Errores al firmar documentos
3. **CertificateException**: Certificado inválido o expirado
4. **TimeoutException**: Timeout de conexión con SUNAT

Todos los errores retornan un objeto `ApiError` con:
- Timestamp
- Código HTTP
- Mensaje descriptivo
- Path del endpoint

## 📚 Códigos de Tipo de Documento

- `01` - Factura
- `03` - Boleta de Venta
- `07` - Nota de Crédito
- `08` - Nota de Débito

## 📚 Códigos de Tipo de IGV

- `10` - Gravado - Operación Onerosa
- `20` - Exonerado - Operación Onerosa
- `30` - Inafecto - Operación Onerosa

## 🧪 Testing

Para probar en ambiente BETA de SUNAT:

1. Usa las credenciales de prueba: `MODDATOS` / `MODDATOS`
2. RUC de prueba: `20000000001`
3. Certificado de prueba proporcionado por SUNAT

## 📖 Referencias

- [Project OpenUBL](https://project-openubl.github.io/)
- [SUNAT - Facturación Electrónica](https://cpe.sunat.gob.pe/)
- [UBL 2.1 Standard](http://docs.oasis-open.org/ubl/UBL-2.1.html)

## 🤝 Soporte

Para problemas o consultas sobre la integración, contacta al equipo de desarrollo.
