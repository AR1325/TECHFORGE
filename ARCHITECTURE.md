# Arquitectura

El proyecto sigue una separación ligera por capas para que la app no dependa de una sola pantalla enorme.

```text
com.checo.techforge
├── MainActivity.kt
├── data
│   ├── Entities.kt
│   ├── Daos.kt
│   ├── TechForgeDatabase.kt
│   └── TechForgeRepository.kt
├── domain
│   ├── DeviceSnapshot.kt
│   └── FileUtilities.kt
└── ui
    ├── Theme.kt
    ├── TechForgeApp.kt
    ├── MainScreens.kt
    └── UtilityScreens.kt
```

## Reglas de diseño

1. UI unidireccional: Compose observa Flow desde la base de datos y las acciones pasan al repositorio.
2. Una fuente de verdad: Room conserva los registros persistentes.
3. Permisos mínimos: no se pide acceso amplio al almacenamiento.
4. Operaciones de archivo en IO: hashing, lectura de logs y respaldos se ejecutan fuera del hilo principal.
5. Restauración atómica: primero se parsea y valida el respaldo; después se reemplazan tablas dentro de una transacción Room.
6. Procesamiento acotado: SHA-256 se calcula por bloques; la lectura de registros tiene un máximo explícito.
7. Comandos pasivos: la biblioteca copia texto al portapapeles, nunca inicia procesos del sistema.

## Crecimiento propuesto

Las próximas extensiones pueden incorporar migraciones de Room, pruebas instrumentadas de importación/exportación, filtros y etiquetas persistentes, y una integración opcional con Shizuku.
