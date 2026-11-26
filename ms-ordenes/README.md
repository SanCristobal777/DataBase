# ms-ordenes

Microservicio de órdenes de trabajo de ACIS. La carpeta `DataBase` funciona como `backend`; `ms-usuarios` y `ms-ordenes` son proyectos hermanos y posteriormente `ms-catalogos` se ubicará en la misma carpeta.

## Ejecución

Iniciar MySQL/XAMPP y ejecutar `..\mvnw.cmd spring-boot:run` desde este directorio (o `mvn spring-boot:run` si Maven está instalado). Usa el puerto 8082 y la base `acis_ordenes`. La URL, usuario y contraseña se pueden configurar con `DB_URL`, `DB_USERNAME` y `DB_PASSWORD`.

## Identidad local

Este servicio no tiene tabla de usuarios. Recibe `X-Usuario-Id` y `X-Usuario-Rol`; es una simplificación local sin JWT y el rol se valida en el service/reglas de estados. En producción se reemplazará por claims de un token.

## Evidencia de CHECK_OUT

Se recibe `cantidadFotosEvidencia` y se exige un valor mínimo de 1. Es una simplificación local: en una versión posterior se validará la evidencia contra `ms-catalogos`.

## Reglas

El SLA es URGENTE=4, ALTA=8, MEDIA=24 y BAJA=72 horas. Un técnico puede tener como máximo 6 OT por fecha programada. Cada transición crea un evento en la bitácora.
