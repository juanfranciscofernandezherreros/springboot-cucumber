@auth
Feature: Flujo de Autenticación v2

  Scenario: Ciclo completo de Login y Logout exitoso
    # 1. Login para obtener el token
    When v2 envío una petición POST a "/auth/login" con el cuerpo:
      """
      {
        "email": "user2@test.com",
        "password": "user123"
      }
      """
    Then v2 el código de estado debe ser 200
    And v2 la respuesta debe contener un token JWT

    # 2. Logout usando el token obtenido automáticamente
    When v2 envío una petición POST para cerrar sesión a "/auth/logout"
    Then v2 el código de estado debe ser 200