# Pollito al Rescate — Asistente de tareas (v1.2.1)

## Stack
- **Backend**: Node.js + Express + PostgreSQL (Neon)
- **Backend deploy**: Render (`https://asistente-backend-ui81.onrender.com/`)
- **App Android**: Kotlin + Jetpack Compose + Material 3 (MVVM)
- **IA**: Groq Llama 3.3 70B (extracción de tareas, gratis)
- **Voz**: Google Speech Intent (ACTION_RECOGNIZE_SPEECH) — NO usar SpeechRecognizer directo
- **Repo**: `charlymelondev/asistente-personal` (público)

## Estructura
```
Asistente/
├── backend/
│   ├── src/
│   │   ├── routes/       # audio.js, text.js, tasks.js, version.js
│   │   ├── services/     # whisper.js, extractor.js, tasks.js, scheduler.js
│   │   ├── prompts/      # extract-tasks.txt (prompt para Groq)
│   │   ├── middleware/    # auth.js
│   │   └── config/       # db.js
│   └── render.yaml       # Blueprint para deploy en Render
│
├── android/
│   └── app/src/main/java/com/carlos/asistente/
│       ├── data/
│       │   ├── remote/   # ApiService (Retrofit), ApiClient, DTOs (incl VersionResponse)
│       │   └── repository/
│       ├── ui/
│       │   ├── screens/  # home, agenda, tasks, detail
│       │   ├── components/ # TaskCard, NewTaskSheet, AnimatedAvatar,
│       │   │               # CelebrationOverlay, CelebrationManager, UpdateChecker
│       │   ├── navigation/ # NavGraph (3 tabs: Inicio, Agenda, Tareas)
│       │   └── theme/
│       └── assets/       # sticker.png, sticker_create.png, sticker_done.png
│
└── release.sh            # Script para crear releases (./release.sh 1.2.1)
```

## App — Características
- **Nombre**: "Pollito al rescate"
- **Icono**: Adaptive icon — foto niño con gafas (foreground PNG + fondo azul)
- **Saludo**: "Hola Papí, ¿en qué puedo ayudarte hoy?"
- **Inicio**: Muestra TODAS las tareas pendientes + contador
- **Navegación**: 3 pestañas (Inicio, Agenda, Tareas)
- **Agenda**: Calendario mensual navegable con flechas
- **Filtros**: Solo Pendientes y Completadas
- **Sin categorías/tags**
- **Stickers animados**:
  - Al crear tarea: foto gafas + "¡A por ello Papí!"
  - Al completar tarea: foto toalla + "¡Bien hecho Papí!" (desde cualquier pantalla)
- **Voz**: Google Speech Intent (funciona en todos los móviles, sin permisos)
- **Auto-update**: GET /api/version + UpdateChecker al abrir app
- **DatePicker**: Asignar/cambiar fecha desde detalle de tarea

## Convenciones
- IDs BIGINT → `String` en Kotlin y JS
- Timezone: `Europe/Madrid`
- Auth: header `X-API-Key` en rutas protegidas
- BASE_URL configurable en `build.gradle.kts`
- Extractor SIEMPRE crea al menos 1 tarea (nunca array vacío)

## API Endpoints
- POST /api/text — Texto → tareas (principal)
- POST /api/audio — Audio → transcripción → tareas (legacy)
- GET /api/tasks — Listar (filtros: status, category, from, to)
- GET /api/tasks/today | /week | /overdue | /summary
- PATCH /api/tasks/:id — Actualizar
- DELETE /api/tasks/:id — Eliminar
- GET /api/version — Versión actual + URL descarga APK (sin auth)
- GET /api/health — Health check (sin auth)

## Comandos
```bash
# Backend local
cd backend && npm run dev

# Compilar APK
cd android && ./gradlew assembleDebug

# Release completa (compila + GitHub Release + push backend)
./release.sh 1.2.1

# Instalar en emulador
adb install -r android/app/build/outputs/apk/debug/app-debug.apk
```

## Pendiente
- Publicar en Google Play Store (ver memory: project_pollito_playstore.md)
- Rediseño UI completo (colores, tarjetas con swipe, animaciones)
