# TECHFORGE Mobile

**TECHFORGE** es una aplicación Android nativa para organizar trabajo técnico desde el teléfono. Esta primera entrega es un MVP funcional a nivel de código fuente, centrado en almacenamiento local y herramientas de inspección seguras.

## Funciones incluidas

- Centro de operaciones, proyectos y bitácora técnica.
- Diagnóstico Android e inventario de dispositivos.
- Biblioteca de comandos ADB, Linux y PowerShell. Solo copia comandos, nunca los ejecuta.
- Cálculo de SHA-256 de archivos seleccionados por el usuario.
- Analizador de registros de texto con límites de lectura.
- Respaldos JSON con validación e importación transaccional.

## Privacidad

- Sin cuentas, anuncios ni servicios remotos.
- Sin permiso de Internet solicitado por la app.
- Datos locales en Room.
- Archivos elegidos mediante Storage Access Framework.
- No ejecuta comandos shell.

## Stack

Kotlin · Jetpack Compose · Material 3 · Room · Coroutines

## Requisitos

JDK 17, Gradle 8.13, Android SDK Platform 36, Android Gradle Plugin 8.13.2 y Kotlin 2.3.21.

## Estado de compilación

Versión de proyecto: **0.1.0**. El código fuente está preparado, pero el APK aún no se ha compilado. El workflow de GitHub Actions está incluido para generar el APK de depuración cuando el proyecto esté publicado.
