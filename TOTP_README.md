# TOTP Two-Factor Authentication

Este proyecto ahora incluye autenticación de dos factores (2FA) usando TOTP (Time-based One-Time Password).

## Características

- Generación de secretos Base32
- Generación de códigos QR para configuración fácil
- Validación de códigos TOTP de 6 dígitos
- Integración completa con el flujo de autenticación existente

## Endpoints API

### 1. Configurar TOTP
**POST** `/auth/totp/setup`

Genera un secreto TOTP y una URL de código QR para el usuario autenticado.

**Headers:**
```
Authorization: Bearer {token}
```

**Respuesta:**
```json
{
  "secret": "JBSWY3DPEHPK3PXP",
  "qrCodeUri": "otpauth://totp/SpringBoot-Security:user@example.com?secret=JBSWY3DPEHPK3PXP&issuer=SpringBoot-Security&algorithm=SHA1&digits=6&period=30"
}
```

### 2. Habilitar TOTP
**POST** `/auth/totp/enable`

Habilita TOTP después de verificar el código generado por la aplicación de autenticación.

**Headers:**
```
Authorization: Bearer {token}
```

**Body:**
```json
{
  "code": "123456"
}
```

### 3. Deshabilitar TOTP
**POST** `/auth/totp/disable`

Deshabilita TOTP para el usuario autenticado.

**Headers:**
```
Authorization: Bearer {token}
```

### 4. Verificar estado de TOTP
**GET** `/auth/totp/status`

Verifica si TOTP está habilitado para el usuario autenticado.

**Headers:**
```
Authorization: Bearer {token}
```

**Respuesta:**
```json
{
  "totpEnabled": true
}
```

### 5. Login con TOTP
**POST** `/auth/totp/login`

Inicia sesión con credenciales y código TOTP (cuando TOTP está habilitado).

**Body:**
```json
{
  "email": "user@example.com",
  "password": "password123",
  "totpCode": "123456"
}
```

**Respuesta:**
```json
{
  "accessToken": "eyJhbGc...",
  "refreshToken": "eyJhbGc..."
}
```

## Flujo de uso

### Para configurar TOTP por primera vez:

1. **Usuario se autentica** con el endpoint `/auth/login` normal
2. **Obtener secreto y QR**: Llamar a `/auth/totp/setup` con el token de autenticación
3. **Escanear QR**: El usuario escanea el código QR con una aplicación de autenticación (Google Authenticator, Authy, etc.)
4. **Habilitar TOTP**: El usuario envía el código de 6 dígitos generado por la app a `/auth/totp/enable`

### Para login con TOTP habilitado:

1. El usuario usa el endpoint `/auth/totp/login` proporcionando email, contraseña y código TOTP
2. Si las credenciales y el código TOTP son correctos, recibe los tokens de acceso

## Aplicaciones compatibles

- Google Authenticator (iOS/Android)
- Microsoft Authenticator (iOS/Android)
- Authy (iOS/Android/Desktop)
- 1Password
- Bitwarden
- Cualquier aplicación compatible con TOTP/RFC 6238

## Tecnología

- **Librería**: `dev.samstevens.totp:totp:1.7.1`
- **Algoritmo**: SHA1
- **Dígitos**: 6
- **Periodo**: 30 segundos
- **Estándar**: RFC 6238 (TOTP)

## Seguridad

- Los secretos TOTP se almacenan de forma segura en la base de datos
- Los códigos tienen una ventana de validez de 30 segundos
- Los intentos fallidos de TOTP cuentan para el sistema de bloqueo de cuentas
- La URL del código QR incluye codificación URL para manejar caracteres especiales en emails

## Schema de Base de Datos

Se han añadido dos campos a la tabla `users`:

```sql
ALTER TABLE users ADD COLUMN totp_secret VARCHAR(255);
ALTER TABLE users ADD COLUMN totp_enabled BOOLEAN DEFAULT false;
```

## Ejemplo de integración con frontend

```javascript
// 1. Configurar TOTP
const setupResponse = await fetch('/auth/totp/setup', {
  headers: {
    'Authorization': `Bearer ${accessToken}`
  }
});
const { secret, qrCodeUri } = await setupResponse.json();

// 2. Mostrar QR al usuario (puede usar qrCodeUri directamente en un <img>)
// O generar el QR en el frontend usando la URL

// 3. Usuario escanea y proporciona código
const code = getUserInputCode(); // "123456"
await fetch('/auth/totp/enable', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${accessToken}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({ code })
});

// 4. En logins futuros
const loginResponse = await fetch('/auth/totp/login', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    email: 'user@example.com',
    password: 'password123',
    totpCode: '123456'
  })
});
```
