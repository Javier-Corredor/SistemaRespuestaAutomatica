# Guía de Uso de JWT en el Proyecto RespuestaAutomatica

## Descripción General

Se ha implementado un sistema completo de autenticación con JWT (JSON Web Tokens) en el proyecto. El sistema incluye:

- **Access Tokens**: Tokens de corta duración (20 minutos por defecto) para acceso a recursos protegidos
- **Refresh Tokens**: Tokens de larga duración (1 día por defecto) para renovar access tokens
- **Filtro de Autenticación**: Intercepta todas las peticiones y valida los tokens
- **Endpoints de Autenticación**: Login, refresh y validación de tokens

## Configuración

Las configuraciones JWT se encuentran en `application.properties`:

```properties
# JWT Configuration
app.jwt.secret=mySecretKeyForJWTTokenGenerationAndValidationPurposesOnly123456789ChangeThisInProduction
app.jwt.access-token-expiration=1200          # 20 minutos en segundos
app.jwt.refresh-token-expiration=86400        # 1 día en segundos
```

### Importante
⚠️ Cambiar la clave secreta (`app.jwt.secret`) en producción por una más segura.

## Endpoints de Autenticación

### 1. Login
**POST** `/auth/login`

Autentica un usuario y devuelve access token y refresh token.

**Request:**
```json
{
  "username": "usuario",
  "password": "contraseña"
}
```

**Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
  "authScheme": "Bearer"
}
```

**Response (401 Unauthorized):**
```
Usuario o contraseña incorrectos
```

---

### 2. Refresh Token
**POST** `/auth/refresh?refreshToken=TOKEN`

Genera un nuevo access token usando el refresh token.

**Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
  "authScheme": "Bearer"
}
```

---

### 3. Validar Token
**POST** `/auth/validate?token=TOKEN`

Valida si un token JWT es válido.

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "username": "usuario",
  "email": "usuario@example.com"
}
```

---

## Endpoints Públicos (Sin Autenticación)

Los siguientes endpoints no requieren autenticación:

- `POST /auth/login` - Login
- `POST /auth/refresh` - Refresh token
- `POST /auth/validate` - Validar token
- `GET /user/listar/` - Listar usuarios
- `POST /user/guardar/` - Guardar usuario (registro)
- `/swagger-ui/**` - Documentación Swagger
- `/v3/api-docs/**` - API docs

## Endpoints Protegidos (Con Autenticación)

Los siguientes endpoints requieren un access token válido en el header:

- `POST /user/actualizar/` - Actualizar usuario
- `DELETE /user/eliminar/{id}` - Eliminar usuario

## Cómo Usar la Autenticación

### Paso 1: Registrar un Usuario

```bash
curl -X POST http://localhost:8081/user/guardar/ \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "username": "juan",
    "email": "juan@example.com",
    "password": "password123"
  }'
```

### Paso 2: Obtener el Access Token

```bash
curl -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "juan",
    "password": "password123"
  }'
```

Respuesta:
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9...",
  "authScheme": "Bearer"
}
```

### Paso 3: Usar el Access Token en Peticiones Protegidas

```bash
curl -X POST http://localhost:8081/user/actualizar/ \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9..." \
  -d '{
    "id": 1,
    "username": "juan",
    "email": "juan.nuevo@example.com",
    "password": "newpassword123"
  }'
```

### Paso 4: Renovar el Access Token (Opcional)

Cuando el access token esté a punto de expirar, usar el refresh token:

```bash
curl -X POST 'http://localhost:8081/auth/refresh?refreshToken=eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9...'
```

## Estructura de los Tokens

### Claims del Token

Cada token contiene los siguientes claims:

```json
{
  "sub": "juan",              // username (subject)
  "email": "juan@example.com", // email del usuario
  "tokenType": "access",      // tipo de token: "access" o "refresh"
  "iat": 1699999999,          // issued at (fecha de emisión)
  "exp": 1700000000           // expiration (fecha de expiración)
}
```

## Clases Implementadas

### 1. JwtTokenProvider
Ubicación: `Security/JwtTokenProvider.java`

Responsable de:
- Generar access tokens y refresh tokens
- Validar tokens
- Extraer información del token (username, email)
- Gestionar claves de firma

### 2. JwtAuthenticationFilter
Ubicación: `Security/JwtAuthenticationFilter.java`

Responsable de:
- Interceptar peticiones HTTP
- Extraer el token del header Authorization
- Validar el token
- Establecer el contexto de autenticación

### 3. AuthController
Ubicación: `Controller/AuthController.java`

Endpoints:
- `POST /auth/login` - Autenticación
- `POST /auth/refresh` - Renovar token
- `POST /auth/validate` - Validar token

### 4. SecurityConfig
Ubicación: `Config/SecurityConfig.java`

Configuración de Spring Security:
- Desactivar CSRF (para APIs REST)
- Sesiones sin estado (STATELESS)
- Configurar filtros de autenticación
- Establecer rutas públicas y protegidas

## Entidades DTO Utilizadas

### LoginRequest
```java
{
  "username": "string",
  "password": "string"
}
```

### AuthTokenPackage
```java
{
  "accessToken": "string",
  "refreshToken": "string",
  "authScheme": "Bearer"
}
```

### AuthResponse
```java
{
  "token": "string",
  "type": "Bearer",
  "username": "string",
  "email": "string"
}
```

## Manejo de Errores

### Token Inválido (401 Unauthorized)
```
Token inválido
```

### Token Expirado
El token expira automáticamente según la configuración. Usar el refresh token para obtener uno nuevo.

### Credenciales Incorrectas
```
Usuario o contraseña incorrectos
```

## Mejoras de Seguridad

Para producción, se recomienda:

1. **Cambiar la clave secreta**: La clave actual es solo para desarrollo.
2. **Usar HTTPS**: Siempre transmitir tokens sobre HTTPS.
3. **Token Blacklist**: Implementar una lista negra de tokens revocados.
4. **Expiración Más Corta**: Considerar reducir la duración del access token.
5. **Renovación Segura**: Implementar un endpoint seguro para renovar tokens.
6. **Rotación de Claves**: Cambiar la clave secreta regularmente.

## Pruebas con Postman o Insomnia

1. **Registrar usuario**:
   - Método: POST
   - URL: `http://localhost:8081/user/guardar/`
   - Body (JSON):
   ```json
   {
     "id": 1,
     "username": "testuser",
     "email": "test@example.com",
     "password": "testpass123"
   }
   ```

2. **Hacer login**:
   - Método: POST
   - URL: `http://localhost:8081/auth/login`
   - Body (JSON):
   ```json
   {
     "username": "testuser",
     "password": "testpass123"
   }
   ```

3. **Usar el access token**:
   - Agregar header: `Authorization: Bearer <accessToken>`
   - Hacer petición a endpoint protegido

## Ejecución del Proyecto

```bash
./mvnw clean compile
./mvnw spring-boot:run
```

El servidor estará disponible en: `http://localhost:8081`

Swagger UI: `http://localhost:8081/swagger-ui.html`
