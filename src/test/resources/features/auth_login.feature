Feature: Gestión de usuarios v2

  @auth @login
  Scenario: Login dinámico
    When v2 envío una petición POST a "/auth/login" con el cuerpo:
      """
      {
        "email": "user2@test.com",
        "password": "user123"
      }
      """
    Then v2 el código de estado debe ser 200
    And v2 la respuesta debe contener un token JWT

  @auth @register
  Scenario: Registro de nuevo usuario
    When v2 envío una petición POST a "/auth/register" con el cuerpo:
      """
      {
        "name": "Nuevo Usuario",
        "email": "jnfz92@gmail.com",
        "password": "password123",
        "role": "USER"
      }
      """
    Then v2 el código de estado debe ser 201