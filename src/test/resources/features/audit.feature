@audit @admin
Feature: Auditoría del Sistema

  Background:
    # Logueamos al administrador para obtener el token necesario
    Given v2 envío una petición POST a "/auth/login" con el cuerpo:
      """
      {
        "email": "admin@test.com",
        "password": "admin123"
      }
      """
    Then v2 el código de estado debe ser 200

  Scenario: Consultar historial de auditoría exitosamente como ADMIN
    When v2 envío una petición GET a "/api/v1/admin/audit/history" con autorización
    Then v2 el código de estado debe ser 200
    And v2 la respuesta debe ser una lista de logs de auditoría