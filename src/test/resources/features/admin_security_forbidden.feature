@security @admin_access_denied
Feature: Seguridad del Panel de Administración - Acceso Denegado

  Background: Autenticación exitosa de usuario estándar
    Given v2 envío una petición POST a "/auth/login" con el cuerpo:
      """
      {
        "email": "user4@test.com",
        "password": "user123"
      }
      """
    # El login debe ser exitoso para obtener el Token JWT
    Then el código de estado debe serlo 200

  @forbidden_access
  Scenario Outline: Intento de acceso a administración por ROLE_USER
    When v3 envío una petición <metodo> a "<endpoint>" con autorización
    # Aquí es donde el sistema reconoce al usuario pero le deniega el acceso
    Then el código de estado debe serlo 403

    Examples:
      | metodo | endpoint                           | descripcion                     |
      | POST   | /api/v1/admin/create-user          | Crear usuario                   |
      | GET    | /api/v1/admin/users                | Listar todos los usuarios       |
      | GET    | /api/v1/admin/locked-users         | Listar usuarios bloqueados      |
      | GET    | /api/v1/admin/status?email=t@t.com | Ver estado de un usuario        |
      | POST   | /api/v1/admin/unlock/test@test.com | Desbloquear usuario             |
      | POST   | /api/v1/admin/lock/test@test.com   | Bloquear usuario                |
      | PUT    | /api/v1/admin/update-role          | Cambiar rol                     |
      | PUT    | /api/v1/admin/update/1             | Actualizar datos de usuario     |
      | DELETE | /api/v1/admin/delete/1             | Eliminar usuario                |
      | GET    | /api/v1/admin/stats                | Ver estadísticas globales       |