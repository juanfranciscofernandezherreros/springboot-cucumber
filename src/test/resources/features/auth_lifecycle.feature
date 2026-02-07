@auth @complete_flow
Feature: Ciclo de vida completo de autenticación

  Scenario: Flujo completo: Registro, Login, Refresh y Logout
    # 1. Registro
    When v2 envío una petición POST a "/auth/register" con el cuerpo:
      """
      {
        "name": "User Active",
        "email": "user22@test.com",
        "password": "user123",
        "role": "USER"
      }
      """
    Then v2 el código de estado debe ser 201

    # 2. Login
    And v2 envío una petición POST a "/auth/login" con el cuerpo:
      """
      {
        "email": "user22@test.com",
        "password": "user123"
      }
      """
    Then v2 el código de estado debe ser 200

    # 3. Refresh Token (Usa automáticamente el refresh_token capturado en el login)
    And v2 envío una petición POST a "/auth/refresh-token" con el cuerpo: "{}"
    Then v2 el código de estado debe ser 200

    # 4. Logout (Usa el nuevo access_token obtenido en el refresh)
    And v2 envío una petición POST para cerrar sesión a "/auth/logout"
    Then v2 el código de estado debe ser 200