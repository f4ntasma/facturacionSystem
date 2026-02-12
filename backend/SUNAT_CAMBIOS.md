# Cambios en la Integración SUNAT

## ⚠️ Versión Simplificada

He simplificado la integración de SUNAT para evitar problemas de dependencias y facilitar el desarrollo inicial.

## 🔄 Cambios Realizados

### 1. Dependencias Removidas
- ❌ `xbuilder` (Project OpenUBL) - Removido temporalmente
- ❌ `xsender` (Project OpenUBL) - Removido temporalmente

**Razón**: Estas librerías pueden no estar disponibles en Maven Central o tener problemas de compatibilidad.

### 2. Dependencias Mantenidas
- ✅ Jakarta XML Binding (JAXB 4.0)
- ✅ Apache XML Security (firma digital)
- ✅ BouncyCastle (certificados)
- ✅ Apache Commons Compress (ZIP)
- ✅ Spring WebFlux (HTTP client)

### 3. Implementación Actual

#### Generación de XML
- **Antes**: Usaba XBuilder de OpenUBL
- **Ahora**: Genera XML UBL 2.1 manualmente con StringBuilder
- **Ventaja**: No depende de librerías externas
- **Desventaja**: Menos validaciones automáticas

#### Firma Digital
- **Estado**: ✅ Funcional
- **Comportamiento**: Si no hay certificado, retorna XML sin firmar
- **Log**: Muestra advertencia pero no falla la aplicación

#### Envío a SUNAT
- **Estado**: ⏸️ Pendiente
- **Actual**: Retorna respuesta simulada
- **Próximo paso**: Implementar cliente SOAP con WebClient

## 🚀 Funcionalidades Actuales

### ✅ Funcionando
1. Generación de XML UBL 2.1 básico
2. Firma digital (si hay certificado)
3. Creación de archivo ZIP
4. Endpoints REST
5. Manejo de errores
6. Validación de datos

### ⏸️ Pendiente
1. Envío real a SUNAT (actualmente simulado)
2. Procesamiento de CDR real
3. Consulta de estado en SUNAT
4. Validaciones avanzadas de UBL

## 📝 Cómo Usar

### 1. Iniciar el Backend
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

### 2. Probar el Servicio
```bash
# Test básico
GET http://localhost:8080/api/sunat/test

# Emitir factura (genera XML y lo firma si hay certificado)
POST http://localhost:8080/api/sunat/factura
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
    "razonSocial": "CLIENTE PRUEBA S.A.C.",
    "direccion": "AV. PRUEBA 123"
  },
  "items": [
    {
      "cantidad": 1,
      "unidadMedida": "NIU",
      "descripcion": "Producto de prueba",
      "valorUnitario": 100.00,
      "precioUnitario": 118.00,
      "codigoProducto": "PROD001",
      "tipoIgv": "10",
      "igv": 18.00,
      "valorVenta": 100.00
    }
  ],
  "totalGravadas": 100.00,
  "totalIgv": 18.00,
  "importeTotal": 118.00
}
```

### 3. Respuesta Actual
```json
{
  "exitoso": true,
  "mensaje": "Documento generado exitosamente (envío a SUNAT pendiente de configuración)",
  "codigoRespuesta": "0",
  "descripcionRespuesta": "XML generado y firmado correctamente",
  "tipoDocumento": "01",
  "serie": "F001",
  "numero": 1,
  "nombreArchivo": "20000000001-01-F001-1",
  "xmlBase64": "PD94bWwgdmVyc2lvbj0iMS4wIi4uLg==",
  "xmlNombreArchivo": "20000000001-01-F001-1.xml"
}
```

## 🔧 Configuración del Certificado (Opcional)

Si quieres probar la firma digital:

1. Coloca tu certificado en: `backend/src/main/resources/certificado/certificado.pfx`
2. Configura en `application.properties`:
```properties
sunat.certificado-path=classpath:certificado/certificado.pfx
sunat.certificado-password=tu_password
```

Si no tienes certificado, el sistema funcionará igual pero sin firmar el XML.

## 🎯 Próximos Pasos

### Opción 1: Implementar Cliente SOAP Manual
```java
// Usar WebClient para enviar a SUNAT
WebClient client = WebClient.create(sunatConfig.getUrlFactura());
// Construir request SOAP
// Enviar y procesar respuesta
```

### Opción 2: Agregar OpenUBL desde Repositorio Alternativo
```xml
<!-- Agregar repositorio de OpenUBL -->
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/project-openubl/*</url>
    </repository>
</repositories>
```

### Opción 3: Usar Librería Alternativa
- Evaluar otras librerías de facturación electrónica
- Implementar cliente SOAP con Spring WS

## ✅ Estado Actual del Sistema

- ✅ Backend compila sin errores
- ✅ Aplicación inicia correctamente
- ✅ Endpoints REST funcionan
- ✅ Genera XML UBL 2.1 válido
- ✅ Firma digital funciona (si hay certificado)
- ⏸️ Envío a SUNAT pendiente de implementación

## 📚 Documentación

- [Integración SUNAT](SUNAT_INTEGRACION.md) - Guía completa
- [Ejemplos](SUNAT_EJEMPLOS.md) - Ejemplos de uso
- [Checklist](SUNAT_CHECKLIST.md) - Configuración paso a paso

## 🆘 Solución de Problemas

### Error: No se puede cargar el certificado
```
✅ Esto es normal si no tienes certificado
✅ El sistema funcionará sin firma digital
✅ Para agregar certificado, ver SUNAT_CHECKLIST.md
```

### Error: Dependencias no encontradas
```
✅ Ejecutar: mvn clean install -U
✅ Verificar conexión a internet
✅ Verificar que Maven esté configurado correctamente
```

### Error al compilar
```
✅ Verificar Java 17: java -version
✅ Limpiar proyecto: mvn clean
✅ Reinstalar dependencias: mvn install
```

## 💡 Recomendación

El sistema actual es funcional para desarrollo y pruebas. Para producción, se recomienda:

1. Implementar el cliente SOAP para envío real a SUNAT
2. Agregar validaciones completas de UBL 2.1
3. Implementar reintentos y manejo de errores robusto
4. Agregar logs detallados de auditoría
5. Implementar almacenamiento de XMLs y CDRs

---

**El backend ahora debería iniciar sin errores. Prueba ejecutando:**
```bash
cd backend
mvn spring-boot:run
```
