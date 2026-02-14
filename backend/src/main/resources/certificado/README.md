# Certificado Digital para Firma Electrónica

## 📋 Instrucciones

Coloca tu certificado digital `.pfx` en esta carpeta.

### Archivo requerido:
```
certificado.pfx
```

### Configuración en application.properties:
```properties
sunat.certificado-path=classpath:certificado/certificado.pfx
sunat.certificado-password=tu_password_aqui
```

## Obtener Certificado Digital

### Para Ambiente de Pruebas (BETA):
SUNAT proporciona un certificado de prueba que puedes descargar desde:
- Portal SUNAT: https://cpe.sunat.gob.pe/
- Sección: Documentación y Manuales
- Archivo: Certificado de Prueba

### Para Producción:
Debes obtener un certificado digital de una entidad certificadora autorizada:
- eCert Perú
- Reniec
- Otras entidades autorizadas por INDECOPI

## Seguridad

**IMPORTANTE:**
- Nunca subas tu certificado de producción al repositorio Git
- Agrega `*.pfx` al archivo `.gitignore`
- Usa variables de entorno para la contraseña en producción
- Renueva el certificado antes de su vencimiento

## Verificar Certificado

Puedes verificar tu certificado usando el endpoint:
```bash
GET /api/sunat/certificado/info
```

Este endpoint te mostrará:
- Si el certificado está vigente
- Fecha de emisión
- Fecha de vencimiento
- Emisor del certificado
