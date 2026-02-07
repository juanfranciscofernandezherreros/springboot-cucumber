@user @profile
Feature: Gestión completa del perfil de usuario

  Background: Autenticación inicial
    Given v2 envío una petición POST a "/auth/login" con el cuerpo:
      """
      {
        "email": "user4@test.com",
        "password": "user123"
      }
      """
    Then v2 el código de estado debe ser 200

  Scenario: Actualizar datos personales y luego cambiar contraseña
    # 1. Actualizar Perfil (PUT)
    When v2 envío una petición PUT para actualizar mi perfil con el cuerpo:
      """
      {
        "name": "Nuevo Nombre Test",
        "email": "user4@test.com"
      }
      """
    Then v2 el código de estado debe ser 200
    And v2 la respuesta contiene "Nuevo Nombre Test"

    # 2. Cambiar Contraseña (POST)
    When v2 envío una petición POST para cambiar mi contraseña con el cuerpo:
      """
      {
        "email": "user4@test.com",
        "newPassword": "veoveo"
      }
      """
    Then v2 el código de estado debe ser 200
    And v2 la respuesta contiene "Contraseña actualizada"

    # 3. Verificar que seguimos teniendo acceso con los nuevos tokens
    When v2 envío una petición GET a "/api/v1/users/me" con autorización
    Then v2 el código de estado debe ser 200
    And v2 la respuesta contiene "Nuevo Nombre Test"