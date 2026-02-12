@security @rbac
Feature: Verificación de Seguridad y Privilegios Granulares

  # =============================================================
  # TEST 1: El "Muro" de Clase (Role Check)
  # =============================================================
  Scenario: Un usuario sin rol ADMIN es bloqueado totalmente por el cerrojo de clase
    Given v2 envío una petición POST a "/auth/login" con el cuerpo:
      """
      {
        "email": "premium@test.com",
        "password": "user123"
      }
      """
    And v2 la respuesta debe contener un token JWT
    # Intentamos acceder a cualquier ruta del AdminController (ej. stats)
    When v2 envío una petición GET a "/api/v1/admin/stats" con autorización
    Then v2 el código de estado debe ser 403

  # =============================================================
  # TEST 2: El Cerrojo de Método (Authority Check - Negativo)
  # =============================================================
  Scenario: Un ADMIN sin privilegio de borrado no puede eliminar usuarios
    Given v2 envío una petición POST a "/auth/login" con el cuerpo:
      """
      {
        "email": "limited-admin@test.com",
        "password": "admin123"
      }
      """
    When v2 envío una petición DELETE a "/api/v1/admin/delete/5" con autorización
    Then v2 el código de estado debe ser 403

  # =============================================================
  # TEST 3: El Cerrojo de Método (Authority Check - Positivo)
  # =============================================================
  Scenario: Un ADMIN con privilegio de lectura puede ver las estadísticas
    Given v2 envío una petición POST a "/auth/login" con el cuerpo:
      """
      {
        "email": "admin@test.com",
        "password": "admin123"
      }
      """
    And v2 la respuesta debe contener un token JWT
    When v2 envío una petición GET a "/api/v1/admin/stats" con autorización
    Then v2 el código de estado debe ser 200