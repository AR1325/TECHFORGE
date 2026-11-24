# Informe de revisión de esta entrega

## Revisiones realizadas

- Se verificó la estructura de archivos del proyecto y el manifiesto Android.
- Se revisó sintácticamente el código Kotlin disponible con el compilador instalado, pero sin classpath de Android/AndroidX.
- Se ejecutaron pruebas independientes de las utilidades: SHA-256 contra el vector conocido de abc, lectura mayor al límite, menor al límite y exactamente en el límite. Todas pasaron.
- Se revisaron manualmente filtrado de registros y restauración JSON transaccional.
- Se validaron los recursos XML y los scripts Bash.
- Se revisó que el manifiesto no declare permiso de Internet ni permisos generales de almacenamiento.
- Se incorporó un workflow CI para compilar el APK en un entorno Android SDK.

## Bloqueo de compilación en el entorno de preparación

El entorno de preparación no contiene Android SDK, Build Tools ni Gradle instalado. Por ello no fue posible ejecutar assembleDebug, lanzar un emulador ni probar la aplicación en un dispositivo real. No se debe interpretar esta entrega como un APK ya verificado.

## Próxima validación requerida

1. Ejecutar gradle :app:assembleDebug con JDK 17, Android SDK 36 y Gradle 8.13.
2. Resolver cualquier diferencia de tooling que reporte Gradle/KSP.
3. Instalar el APK en un dispositivo Android 8.0+.
4. Probar CRUD, selección de archivos, SHA-256 y exportar/restaurar un respaldo de prueba.
