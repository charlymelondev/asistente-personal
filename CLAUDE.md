# Asistente Personal — Carlos

## Stack
- **Backend**: Node.js + Express (puerto 3001) + PostgreSQL (DB: `asistente`)
- **App Android**: Kotlin + Jetpack Compose + Material 3 (MVVM)
- **IA**: Groq Whisper (transcripción gratis) + Groq Llama 3.3 70B (extracción de tareas gratis)
- **Push**: Firebase Cloud Messaging (pendiente de configurar)

## Estructura
```
Asistente/
├── backend/          # API REST Node.js
│   ├── src/
│   │   ├── routes/   # audio.js, text.js, tasks.js, device.js
│   │   ├── services/ # whisper.js, extractor.js, tasks.js, scheduler.js
│   │   ├── middleware/auth.js
│   │   └── config/db.js
│   └── .env          # API keys (NO commitear)
│
└── android/          # App Kotlin
    └── app/src/main/java/com/carlos/asistente/
        ├── data/     # remote (Retrofit), local (Room), repository
        ├── ui/       # screens (home, agenda, tasks, detail), components, navigation
        ├── audio/    # AudioRecorder (M4A)
        └── push/     # FCM service
```

## Convenciones
- IDs BIGINT → `String` en Kotlin y JS (nunca comparar con ===)
- Timezone: siempre `Europe/Madrid`
- Auth: header `X-API-Key` en todas las rutas protegidas
- Audio: M4A (AAC) → Whisper acepta directamente

## Comandos
```bash
# Backend
cd backend && npm run dev      # Desarrollo con hot reload
cd backend && npm start         # Producción

# Base de datos
PGPASSWORD=postgres psql -U postgres -d asistente  # Conexión directa
```

## API Endpoints
- POST /api/audio — Audio → transcripción → tareas
- POST /api/text — Texto → tareas
- GET /api/tasks — Listar (filtros: status, category, from, to)
- GET /api/tasks/today | /week | /overdue | /summary
- PATCH /api/tasks/:id — Actualizar
- DELETE /api/tasks/:id — Eliminar
- POST /api/device — Registrar FCM token
- GET /api/health — Health check (sin auth)
