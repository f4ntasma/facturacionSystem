# Publicidad automática de Al Toque

Un video vertical por día para Instagram, Facebook y TikTok, escrito por IA, renderizado con la marca Al Toque
y publicado solo después de que lo apruebes desde Telegram.

```
06:00  n8n elige el pilar del día ─► la IA escribe guion y textos ─► el renderizador arma el MP4 (voz + subtítulos)
       ─► te llega a Telegram con 3 botones: ✅ Publicar · 🔄 Rehacer · 🗑 Descartar
       ─► ✅ publica el Reel en Instagram y el video en Facebook y te avisa
```

| Archivo | Qué es |
|---|---|
| `guionista.md` | Instrucciones de la IA: voz de marca, pilares, reglas SUNAT permitidas, datos reales (precios, prueba gratis). |
| `marca.json` | Colores, logo y textos de cierre que usa el video. |
| `build_workflow.py` | Genera el flujo de n8n. Cambias el guionista o la marca → `python marketing/build_workflow.py`. |
| `n8n-video-diario.json` | El flujo listo para importar en n8n. |
| `ejemplo-video.json` | Un guion de ejemplo para probar el renderizador. |

## Aprende de lo que funciona

Cada mañana, antes de escribir, el flujo mide en Instagram los videos publicados hace entre 2 y 90 días
(alcance, me gusta, comentarios, compartidos, guardados y vistas) y les da un puntaje:

```
puntaje = (me gusta + 2 × comentarios + 3 × compartidos + 3 × guardados) ÷ alcance × 1000
```

Compartir y guardar pesan más porque son la señal de que el video le sirvió a alguien. Los videos con menos de 30 de alcance no cuentan.

- La IA recibe los **5 mejores** (gancho, puntos, pilar) como referencia de tono y ritmo, y los **2 peores** para evitar ese enfoque.
- Si un pilar rinde más que los demás, se le sugiere usar su energía en el tema del día.
- **Cada lunes** te llega a Telegram el ranking: los 3 mejores videos y el promedio por pilar.
- Necesita la credencial de Meta (la misma que publica). Mientras no haya videos medidos, el flujo funciona igual que antes.

Lo medido vive en los datos del flujo en n8n (`published` y `aprendizaje`); no hace falta base de datos aparte.

## Pilares (uno por día)

| Día | Pilar | De qué habla |
|---|---|---|
| Lunes | cuaderno | Lo que cuesta llevar todo a mano. |
| Martes | sunat_facil | Una regla SUNAT en simple (boleta con DNI desde S/ 700, 7 días para anular, detracciones…). |
| Miércoles | truco | Una función: Yape/Plin en caja, boleta por WhatsApp, stock solo, DNI que se completa. |
| Jueves | rubro | El problema contado para bodega, ferretería, botica, restaurante… |
| Viernes | antes_despues | Tres pasos a mano contra un toque. |
| Sábado | contador | Para estudios contables: todos sus clientes en una cuenta. |
| Domingo | cuaderno | Otra vuelta al dolor principal. |

La IA no puede inventar testimonios, clientes, cifras ni normas: el guionista solo le permite usar datos reales.

## Ponerlo en marcha (una sola vez)

1. En tu n8n: **Workflows › Import from file** → `n8n-video-diario.json`.
2. Crea estas credenciales (los nodos ya las buscan por nombre):
   - **Clave de IA** (Header Auth): `x-goog-api-key` con una clave gratis de Google AI Studio, o `x-api-key` de Anthropic si en *Configuración* pones `aiProvider: 'claude'`.
   - **Renderizador M&A** (Header Auth): name `Authorization`, value `Bearer <RENDER_TOKEN>`; el token está en Dokploy › ma-company-contenido › ma-renderer › Environment.
   - **Telegram Al Toque**: un bot nuevo creado con @BotFather.
   - **Meta Al Toque (token de página)** (Query Auth): `access_token` = token de larga duración de la página de Facebook de Al Toque (con su Instagram profesional vinculado).
3. En el nodo **Configuración** pon tu `telegramChatId`, el `instagramUserId` y el `facebookPageId`.
4. Toca **Probar ahora**. Cuando te llegue el video a Telegram y te guste, activa el flujo.

TikTok y LinkedIn quedan como nodos desactivados hasta que esas redes aprueben la app (igual que en M&A).

## Canales que no son videos (del estudio de mercado)

- **Google Maps**: busca «ferretería», «botica», «bodega» + distrito; escribe por WhatsApp a los que no tienen sistema.
- **Contadores**: son el aliado con más alcance. Ofréceles el acceso multi-RUC y una comisión por referido.
- **Grupos de Facebook de bodegueros y emprendedores**: contenido útil (cómo emitir boleta, qué es la detracción), nada de spam.
- **WhatsApp Business**: catálogo con los 3 planes, respuestas rápidas y estados con los videos de la semana.
- **Meta Ads**: campaña «mensajes a WhatsApp», S/ 10–15 al día por zona, con el video que mejor funcione en orgánico.
