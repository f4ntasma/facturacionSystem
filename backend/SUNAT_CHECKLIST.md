# ✅ Checklist de Configuración SUNAT

## 📋 Pasos para Activar la Facturación Electrónica

### 1. Obtener Certificado Digital

#### Para Ambiente de Pruebas (BETA):
- [ ] Descargar certificado de prueba de SUNAT
- [ ] URL: https://cpe.sunat.gob.pe/
- [ ] Sección: Documentación y Manuales
- [ ] Archivo: Certificado de Prueba

#### Para Producción:
- [ ] Contratar certificado digital con entidad autorizada:
  - eCert Perú
  - Reniec
  - Otras entidades INDECOPI
- [ ] Descargar certificado en formato .pfx o .p12

### 2. Configurar Certificado en el Proyecto

- [ ] Crear carpeta si no existe: `backend/src/main/resources/certificado/`
- [ ] Copiar tu archivo certificado.pfx a esa carpeta
- [ ] Verificar que el archivo se llame exactamente: `certificado.pfx`
- [ ] Anotar la contraseña del certificado

### 3. Configurar application.properties

Editar: `backend/src/main/resources/application.properties`

#### Credenciales SUNAT:
```properties
# Para BETA (Pruebas)
sunat.ruc=20000000001
sunat.usuario=MODDATOS
sunat.clave=MODDATOS

# Para PRODUCCIÓN (cambiar cuando estés listo)
# sunat.ruc=TU_RUC_REAL
# sunat.usuario=TU_USUARIO_SOL
# sunat.clave=TU_CLAVE_SOL
```

- [ ] Configurar RUC
- [ ] Configurar usuario SOL
- [ ] Configurar clave SOL

#### Certificado:
```properties
sunat.certificado-path=classpath:certificado/certificado.pfx
sunat.certificado-password=TU_PASSWORD_AQUI
```

- [ ] Verificar ruta del certificado
- [ ] Configurar contraseña del certificado

#### Datos de la Empresa:
```properties
sunat.razon-social=TU EMPRESA S.A.C.
sunat.nombre-comercial=TU EMPRESA
sunat.direccion=TU DIRECCION COMPLETA
sunat.ubigeo=150101
sunat.departamento=LIMA
sunat.provincia=LIMA
sunat.distrito=LIMA
```

- [ ] Configurar razón social
- [ ] Configurar nombre comercial
- [ ] Configurar dirección
- [ ] Configurar ubigeo (código de 6 dígitos)
- [ ] Configurar departamento
- [ ] Configurar provincia
- [ ] Configurar distrito

#### Ambiente:
```properties
# false = BETA (Pruebas)
# true = PRODUCCIÓN
sunat.produccion=false
```

- [ ] Configurar ambiente (false para pruebas)

### 4. Verificar Dependencias

- [ ] Verificar que el pom.xml tenga todas las dependencias
- [ ] Ejecutar: `mvn clean install`
- [ ] Verificar que no haya errores de compilación

### 5. Iniciar la Aplicación

```bash
cd backend
mvn spring-boot:run
```

- [ ] Iniciar el backend
- [ ] Verificar que no haya errores en los logs
- [ ] Buscar mensaje: "Certificado digital cargado exitosamente"

### 6. Probar la Configuración

#### Test básico:
```bash
GET http://localhost:8080/api/sunat/test
```

Respuesta esperada:
```json
{
  "servicio": "SUNAT Facturación Electrónica",
  "estado": "Activo",
  "certificado_vigente": true,
  "version": "1.0.0"
}
```

- [ ] Ejecutar test básico
- [ ] Verificar que el servicio esté activo
- [ ] Verificar que el certificado esté vigente

#### Verificar certificado:
```bash
GET http://localhost:8080/api/sunat/certificado/info
Authorization: Bearer {tu_token_jwt}
```

- [ ] Ejecutar verificación de certificado
- [ ] Verificar fechas de vigencia
- [ ] Verificar que no esté expirado

### 7. Emitir Documento de Prueba

#### Obtener token JWT:
```bash
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin"
}
```

- [ ] Obtener token JWT
- [ ] Copiar el token para usar en siguientes requests

#### Emitir factura de prueba:
```bash
POST http://localhost:8080/api/sunat/factura
Authorization: Bearer {tu_token}
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

- [ ] Emitir factura de prueba
- [ ] Verificar respuesta exitosa
- [ ] Verificar que se reciba el CDR de SUNAT

### 8. Verificar Respuesta

Respuesta esperada:
```json
{
  "exitoso": true,
  "mensaje": "Documento aceptado por SUNAT",
  "codigoRespuesta": "0",
  "tipoDocumento": "01",
  "serie": "F001",
  "numero": 1,
  "xmlBase64": "...",
  "cdrBase64": "..."
}
```

- [ ] Verificar que exitoso = true
- [ ] Verificar que se reciba el XML
- [ ] Verificar que se reciba el CDR

### 9. Seguridad

- [ ] Verificar que certificado.pfx NO esté en Git
- [ ] Verificar que .gitignore incluya *.pfx
- [ ] No compartir contraseñas en el código
- [ ] Usar variables de entorno en producción

### 10. Paso a Producción

Cuando estés listo para producción:

- [ ] Cambiar `sunat.produccion=true`
- [ ] Actualizar URLs a producción:
```properties
sunat.url-factura=https://e-factura.sunat.gob.pe/ol-ti-itcpfegem/billService
```
- [ ] Configurar RUC, usuario y clave reales
- [ ] Usar certificado digital de producción
- [ ] Probar en ambiente controlado primero
- [ ] Verificar con SUNAT que tu RUC esté habilitado

## 🆘 Solución de Problemas

### Error: Certificado no encontrado
```
✅ Solución:
- Verificar que el archivo esté en: src/main/resources/certificado/certificado.pfx
- Verificar que el nombre sea exacto: certificado.pfx
- Verificar la ruta en application.properties
```

### Error: Certificado expirado
```
✅ Solución:
- Verificar fechas con: GET /api/sunat/certificado/info
- Renovar el certificado digital
- Reemplazar el archivo .pfx
- Reiniciar la aplicación
```

### Error: Credenciales inválidas
```
✅ Solución:
- Verificar RUC, usuario y clave en application.properties
- Para BETA usar: MODDATOS / MODDATOS
- Para producción usar credenciales SOL reales
```

### Error: No se puede conectar con SUNAT
```
✅ Solución:
- Verificar conexión a internet
- Verificar que las URLs sean correctas
- Verificar firewall/proxy
- Probar con: GET /api/sunat/test
```

### Error: Documento rechazado
```
✅ Solución:
- Revisar el código de error en la respuesta
- Consultar tabla de errores SUNAT
- Verificar datos del cliente (RUC/DNI válido)
- Verificar totales y cálculos
```

## 📚 Recursos Adicionales

- [Documentación Completa](SUNAT_INTEGRACION.md)
- [Ejemplos de Uso](SUNAT_EJEMPLOS.md)
- [Resumen Técnico](SUNAT_RESUMEN.md)
- [Portal SUNAT](https://cpe.sunat.gob.pe/)

## ✅ Checklist Final

- [ ] Certificado configurado
- [ ] application.properties actualizado
- [ ] Aplicación inicia sin errores
- [ ] Test básico exitoso
- [ ] Certificado vigente
- [ ] Factura de prueba emitida
- [ ] CDR recibido de SUNAT
- [ ] Documentación revisada
- [ ] Listo para usar en desarrollo

---

**¡Felicidades! Tu sistema de facturación electrónica SUNAT está configurado y listo para usar.**
