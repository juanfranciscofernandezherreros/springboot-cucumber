@admin @invitations @lists
Feature: Consulta de listados de invitaciones para Administradores

  Background: Autenticación de Administrador
    Given v2 envío una petición POST a "/auth/login" con el cuerpo:
      """
      {
        "email": "admin_read@test.com",
        "password": "admin123"
      }
      """
    Then v2 el código de estado debe ser 200
    And v2 la respuesta debe contener un token JWT

  Scenario: Consultar todos los listados y verificar que contienen datos
    # 1. Estados posibles
    When v2 envío una petición GET a "/api/v1/admin/invitations/statuses" con autorización
    Then v2 el código de estado debe ser 200
    And v2 la respuesta debe ser una lista con elementos

    # 2. Invitaciones pendientes (debería haber al menos las 2 del seed CSV: test-invitations.csv)
    When v2 envío una petición GET a "/api/v1/admin/invitations/pending" con autorización
    Then v2 el código de estado debe ser 200
    And v2 la respuesta debe ser una lista con elementos

    # 3. Historial (Aceptadas, Rechazadas, etc.)
    When v2 envío una petición GET a "/api/v1/admin/invitations/history" con autorización
    Then v2 el código de estado debe ser 200
    And v2 la respuesta debe ser una lista con elementos

    # 4. Listado Maestro
    When v2 envío una petición GET a "/api/v1/admin/invitations/all" con autorización
    Then v2 el código de estado debe ser 200
    And v2 la respuesta debe ser una lista con elementos

  Scenario: Filtrado por estados y validación de resultados
    When v2 envío una petición GET a "/api/v1/admin/invitations/all?statuses=PENDING" con autorización
    Then v2 el código de estado debe ser 200
    And v2 la respuesta debe ser una lista con elementos
