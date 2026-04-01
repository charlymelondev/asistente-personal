# Pollito al Rescate — Asistente de tareas

## Stack
- **Backend**: Node.js + Express + PostgreSQL (Neon)
- **Backend deploy**: Render (`https://asistente-backend-ui81.onrender.com/`)
- **App Android**: Kotlin + Jetpack Compose + Material 3 (MVVM)
- **IA**: Groq Llama 3.3 70B (extracción de tareas, gratis)
- **Audio**: Android SpeechRecognizer nativo (on-device, instantáneo)
- **Repo**: `charlymelondev/asistente-personal`

## Estructura
```
Asistente/
├── backend/
│   ├── src/
│   │   ├── routes/       # audio.js, text.js, tasks.js, device.js
│   │   ├── services/     # whisper.js, extractor.js, tasks.js, scheduler.js
│   │   ├── middleware/    # auth.js
│   │   └── config/       # db.js
│   └── render.yaml       # Blueprint para deploy en Render
│
└── android/
    └── app/src/main/java/com/carlos/asistente/
        ├── data/
        │   ├── remote/   # ApiService (Retrofit), ApiClient, DTOs
        │   ├── audio/    # SpeechRecognizerHelper (voz nativa Android)
        │   └── repository/
        ├── ui/
        │   ├── screens/  # home, agenda, tasks, detail (SIN inbox)
        │   ├── components/ # TaskCard, NewTaskSheet, AnimatedAvatar,
        │   │               # CelebrationOverlay, CelebrationManager
        │   ├── navigation/ # NavGraph (3 tabs: Inicio, Agenda, Tareas)
        │   └── theme/
        └── push/         # FCM service (pendiente)
```

## App — Características actuales
- **Nombre**: "Pollito al rescate"
- **Icono**: Foto del niño con gafas de sol
- **Saludo**: "Hola Papí, ¿en qué puedo ayudarte hoy?"
- **Navegación**: 3 pestañas (Inicio, Agenda, Tareas) — sin Buzón
- **Filtros**: Solo Pendientes y Completadas (sin "Todas")
- **Sin categorías/tags** en tarjetas ni filtros
- **Stickers animados**:
  - Al crear tarea: foto gafas de sol + "¡A por ello Papí!"
  - Al completar tarea: foto toalla + "¡Bien hecho Papí!"
- **Audio**: SpeechRecognizer nativo de Android (instantáneo, gratis)
- **DatePicker**: Se puede asignar/cambiar fecha desde el detalle de tarea

## Convenciones
- IDs BIGINT → `String` en Kotlin y JS
- Timezone: `Europe/Madrid`
- Auth: header `X-API-Key` en rutas protegidas
- BASE_URL configurable en `build.gradle.kts`

## API Endpoints
- POST /api/text — Texto → tareas (principal)
- POST /api/audio — Audio → transcripción → tareas (legacy, no usado desde app)
- GET /api/tasks — Listar (filtros: status, category, from, to)
- GET /api/tasks/today | /week | /overdue | /summary
- PATCH /api/tasks/:id — Actualizar (título, fecha, estado, etc.)
- DELETE /api/tasks/:id — Eliminar
- GET /api/health — Health check (sin auth)

## Comandos
```bash
# Backend local
cd backend && npm run dev

# Compilar APK
cd android && ./gradlew assembleDebug
# APK en: app/build/outputs/apk/debug/app-debug.apk

# Instalar en emulador
adb install -r app/build/outputs/apk/debug/app-debug.apk
```
