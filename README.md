# Backend ACIS

La carpeta `DataBase` funciona como la raíz de backend. Contiene tres proyectos Spring Boot independientes, cada uno con su puerto y base MySQL.

## Levantar los servicios

Inicia MySQL desde XAMPP con usuario `root` y contraseña vacía. Cada servicio crea su base mediante `createDatabaseIfNotExist=true`.

```powershell
# ms-usuarios, puerto 8081
cd C:\Users\PcCris\Desktop\DataBase
.\mvnw.cmd spring-boot:run

# ms-ordenes, puerto 8082
cd C:\Users\PcCris\Desktop\DataBase\ms-ordenes
..\mvnw.cmd spring-boot:run

# ms-catalogos, puerto 8083
cd C:\Users\PcCris\Desktop\DataBase\ms-catalogos
..\mvnw.cmd spring-boot:run
```

Cada proyecto permite configurar `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`; `ms-catalogos` también permite configurar `UPLOADS_DIR`.

## Bases y tablas principales

| Servicio | Puerto | Base | Tablas |
|---|---:|---|---|
| ms-usuarios | 8081 | acis_usuarios | usuarios, codigos_recuperacion |
| ms-ordenes | 8082 | acis_ordenes | ordenes_trabajo, eventos_orden |
| ms-catalogos | 8083 | acis_catalogos | clientes, equipos, motivos_catalogo, fotos_evidencia |

## Identidad del actor

La identidad viaja en headers como simplificación local, sin JWT. `ms-usuarios` usa `X-Usuario-Id`; `ms-ordenes` y `ms-catalogos` usan `X-Usuario-Id` y `X-Usuario-Rol`. En una versión productiva estos datos deben provenir de claims de un token validado.

## Endpoints

### ms-usuarios

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/usuarios/registro` | Autoregistro de cliente |
| POST | `/api/usuarios/login` | Iniciar sesión |
| GET | `/api/usuarios` | Listar con filtros |
| GET | `/api/usuarios/{id}` | Obtener usuario |
| POST | `/api/usuarios` | Crear usuario administrativo |
| PUT/PATCH | `/api/usuarios/{id}` y subrutas | Editar, rol y estado |
| POST | `/api/usuarios/recuperar` | Generar recuperación |
| POST | `/api/usuarios/restablecer` | Restablecer contraseña |

### ms-ordenes

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/ordenes` | Crear OT |
| GET | `/api/ordenes` | Listar con filtros y visibilidad por rol |
| GET | `/api/ordenes/{id}` | Obtener OT |
| PUT/DELETE | `/api/ordenes/{id}` | Editar o eliminar OT CREADA |
| POST | `/api/ordenes/{id}/acciones` | Ejecutar transición de estado |
| GET | `/api/ordenes/{id}/eventos` | Consultar bitácora |
| GET | `/api/ordenes/{id}/acciones-disponibles` | Consultar acciones permitidas |
| GET | `/api/ordenes/metricas` | Métricas operativas |

### ms-catalogos

| Método | Ruta | Descripción |
|---|---|---|
| POST/GET/PUT/DELETE | `/api/clientes` y `/api/clientes/{id}` | CRUD de clientes con borrado lógico |
| POST/GET/PUT/DELETE | `/api/equipos` y `/api/equipos/{id}` | CRUD de equipos con borrado lógico |
| POST/GET/PUT/DELETE | `/api/motivos` y `/api/motivos/{id}` | CRUD de motivos con borrado lógico |
| POST | `/api/fotos` | Subir JPG/PNG de máximo 5 MB |
| GET | `/api/fotos/orden/{ordenId}` | Listar fotos de una OT |
| GET | `/api/fotos/{id}/archivo` | Descargar imagen |
| DELETE | `/api/fotos/{id}` | Eliminar foto y archivo |
