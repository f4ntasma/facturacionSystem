# Ejemplos de Uso - Facturación Electrónica SUNAT

## 🧪 Ejemplos de Requests

### 1. Emitir Factura Completa

```json
POST /api/sunat/factura
Content-Type: application/json
Authorization: Bearer {tu_token_jwt}

{
  "tipoDocumento": "01",
  "serie": "F001",
  "numero": 1,
  "fechaEmision": "2024-02-08",
  "tipoMoneda": "PEN",
  "cliente": {
    "tipoDocumento": "6",
    "numeroDocumento": "20123456789",
    "razonSocial": "EMPRESA CLIENTE S.A.C.",
    "direccion": "AV. LOS HEROES 456 - LIMA",
    "email": "facturacion@cliente.com"
  },
  "items": [
    {
      "cantidad": 10,
      "unidadMedida": "NIU",
      "descripcion": "Laptop HP Core i7 16GB RAM",
      "valorUnitario": 2500.00,
      "precioUnitario": 2950.00,
      "codigoProducto": "LAP001",
      "descuento": 0,
      "tipoIgv": "10",
      "igv": 450.00,
      "totalImpuestos": 450.00,
      "valorVenta": 25000.00
    },
    {
      "cantidad": 5,
      "unidadMedida": "NIU",
      "descripcion": "Mouse Inalámbrico Logitech",
      "valorUnitario": 50.00,
      "precioUnitario": 59.00,
      "codigoProducto": "MOU001",
      "descuento": 0,
      "tipoIgv": "10",
      "igv": 45.00,
      "totalImpuestos": 45.00,
      "valorVenta": 250.00
    }
  ],
  "totalGravadas": 25250.00,
  "totalExoneradas": 0,
  "totalInafectas": 0,
  "totalIgv": 4545.00,
  "totalDescuentos": 0,
  "totalOtrosCargos": 0,
  "importeTotal": 29795.00,
  "observaciones": "Pago al contado - Entrega inmediata"
}
```

### 2. Emitir Boleta de Venta

```json
POST /api/sunat/boleta
Content-Type: application/json
Authorization: Bearer {tu_token_jwt}

{
  "tipoDocumento": "03",
  "serie": "B001",
  "numero": 1,
  "fechaEmision": "2024-02-08",
  "tipoMoneda": "PEN",
  "cliente": {
    "tipoDocumento": "1",
    "numeroDocumento": "12345678",
    "razonSocial": "JUAN CARLOS PEREZ LOPEZ",
    "direccion": "CALLE LAS FLORES 123 - LIMA"
  },
  "items": [
    {
      "cantidad": 2,
      "unidadMedida": "NIU",
      "descripcion": "Teclado Mecánico RGB",
      "valorUnitario": 150.00,
      "precioUnitario": 177.00,
      "codigoProducto": "TEC001",
      "tipoIgv": "10",
      "igv": 54.00,
      "valorVenta": 300.00
    }
  ],
  "totalGravadas": 300.00,
  "totalIgv": 54.00,
  "importeTotal": 354.00,
  "observaciones": "Venta al público"
}
```

### 3. Factura con Productos Exonerados

```json
POST /api/sunat/factura
Content-Type: application/json
Authorization: Bearer {tu_token_jwt}

{
  "tipoDocumento": "01",
  "serie": "F001",
  "numero": 2,
  "fechaEmision": "2024-02-08",
  "tipoMoneda": "PEN",
  "cliente": {
    "tipoDocumento": "6",
    "numeroDocumento": "20987654321",
    "razonSocial": "HOSPITAL CENTRAL S.A.",
    "direccion": "AV. SALUD 789 - LIMA",
    "email": "compras@hospital.com"
  },
  "items": [
    {
      "cantidad": 100,
      "unidadMedida": "NIU",
      "descripcion": "Medicamento Genérico - Paracetamol 500mg",
      "valorUnitario": 2.00,
      "precioUnitario": 2.00,
      "codigoProducto": "MED001",
      "tipoIgv": "20",
      "igv": 0,
      "valorVenta": 200.00
    },
    {
      "cantidad": 50,
      "unidadMedida": "NIU",
      "descripcion": "Equipo Médico - Termómetro Digital",
      "valorUnitario": 80.00,
      "precioUnitario": 94.40,
      "codigoProducto": "EQU001",
      "tipoIgv": "10",
      "igv": 720.00,
      "valorVenta": 4000.00
    }
  ],
  "totalGravadas": 4000.00,
  "totalExoneradas": 200.00,
  "totalInafectas": 0,
  "totalIgv": 720.00,
  "importeTotal": 4920.00,
  "observaciones": "Productos para uso hospitalario"
}
```

### 4. Factura con Descuentos

```json
POST /api/sunat/factura
Content-Type: application/json
Authorization: Bearer {tu_token_jwt}

{
  "tipoDocumento": "01",
  "serie": "F001",
  "numero": 3,
  "fechaEmision": "2024-02-08",
  "tipoMoneda": "PEN",
  "cliente": {
    "tipoDocumento": "6",
    "numeroDocumento": "20555666777",
    "razonSocial": "DISTRIBUIDORA MAYORISTA S.A.C.",
    "direccion": "JR. COMERCIO 321 - LIMA",
    "email": "ventas@mayorista.com"
  },
  "items": [
    {
      "cantidad": 100,
      "unidadMedida": "NIU",
      "descripcion": "Producto al por mayor",
      "valorUnitario": 100.00,
      "precioUnitario": 118.00,
      "codigoProducto": "PROD001",
      "descuento": 1000.00,
      "tipoIgv": "10",
      "igv": 1620.00,
      "valorVenta": 9000.00
    }
  ],
  "totalGravadas": 9000.00,
  "totalIgv": 1620.00,
  "totalDescuentos": 1000.00,
  "importeTotal": 10620.00,
  "observaciones": "Descuento por volumen aplicado"
}
```

## 📊 Respuestas Esperadas

### Respuesta Exitosa

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
  "hashCpe": null,
  "codigoBarras": null,
  "cdrBase64": "UEsDBBQACAgIAA...",
  "cdrNombreArchivo": "R-20000000001-01-F001-1.xml",
  "xmlBase64": "PD94bWwgdmVyc2lvbj0iMS4wIi...",
  "xmlNombreArchivo": "20000000001-01-F001-1.xml",
  "codigoError": null,
  "mensajeError": null
}
```

### Respuesta con Error

```json
{
  "exitoso": false,
  "mensaje": "Documento rechazado por SUNAT",
  "codigoRespuesta": null,
  "descripcionRespuesta": null,
  "tipoDocumento": "01",
  "serie": "F001",
  "numero": 1,
  "nombreArchivo": "20000000001-01-F001-1",
  "codigoError": "2324",
  "mensajeError": "El número de documento del cliente no es válido"
}
```

## 🔍 Consultar Estado de Documento

```bash
GET /api/sunat/consultar/01/F001/1
Authorization: Bearer {tu_token_jwt}
```

## 🔐 Verificar Certificado Digital

```bash
GET /api/sunat/certificado/info
Authorization: Bearer {tu_token_jwt}
```

Respuesta:
```json
{
  "vigente": true,
  "detalles": "Emisor: CN=SUNAT...\nSujeto: CN=20000000001...\nVálido desde: 2023-01-01\nVálido hasta: 2025-12-31"
}
```

## 🧪 Test de Configuración

```bash
GET /api/sunat/test
```

Respuesta:
```json
{
  "servicio": "SUNAT Facturación Electrónica",
  "estado": "Activo",
  "certificado_vigente": true,
  "version": "1.0.0"
}
```

## 📝 Códigos de Referencia

### Tipos de Documento de Identidad (Catalog 6)
- `1` - DNI
- `6` - RUC
- `4` - Carnet de Extranjería
- `7` - Pasaporte

### Tipos de Comprobante (Catalog 1)
- `01` - Factura
- `03` - Boleta de Venta
- `07` - Nota de Crédito
- `08` - Nota de Débito

### Tipos de IGV (Catalog 7)
- `10` - Gravado - Operación Onerosa
- `20` - Exonerado - Operación Onerosa
- `30` - Inafecto - Operación Onerosa
- `40` - Exportación

### Unidades de Medida Comunes
- `NIU` - Unidad
- `ZZ` - Unidad (Servicios)
- `KGM` - Kilogramo
- `MTR` - Metro
- `LTR` - Litro
- `HUR` - Hora
- `DAY` - Día

## 🚀 Flujo Completo de Emisión

1. **Preparar datos** del comprobante
2. **Enviar request** a `/api/sunat/factura` o `/api/sunat/boleta`
3. **Sistema construye** el XML UBL 2.1
4. **Sistema firma** el XML con el certificado digital
5. **Sistema comprime** el XML en formato ZIP
6. **Sistema envía** a SUNAT
7. **SUNAT procesa** y retorna CDR (Constancia de Recepción)
8. **Sistema retorna** respuesta con XML y CDR en Base64

## 💾 Guardar Archivos

Los archivos XML y CDR vienen en Base64. Para guardarlos:

```javascript
// Decodificar Base64 y guardar
const xmlContent = atob(response.xmlBase64);
const blob = new Blob([xmlContent], { type: 'application/xml' });
const url = window.URL.createObjectURL(blob);
const a = document.createElement('a');
a.href = url;
a.download = response.xmlNombreArchivo;
a.click();
```

## ⚠️ Errores Comunes

### Error: Certificado no encontrado
```
Solución: Verifica que el archivo .pfx esté en src/main/resources/certificado/
```

### Error: Certificado expirado
```
Solución: Renueva tu certificado digital
```

### Error: RUC no autorizado
```
Solución: Verifica que tu RUC esté afiliado a SUNAT para facturación electrónica
```

### Error: Serie no válida
```
Solución: Las facturas deben usar series que empiecen con F (F001, F002, etc.)
           Las boletas deben usar series que empiecen con B (B001, B002, etc.)
```
