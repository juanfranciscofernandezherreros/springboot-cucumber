@admin @invitations @crud
Feature: Ciclo de vida CRUD de Invitaciones

  Background: Autenticación
    Given v2 envío una petición POST a "/auth/login" con el cuerpo:
      """
      {
        "email": "admin@test.com",
        "password": "admin123"
      }
      """
    Then v2 el código de estado debe ser 200

  Scenario: Crear, actualizar, cambiar estado y eliminar una invitación
    # 1. CREACIÓN (POST)
    When v2 envío una petición POST a "/api/v1/admin/invitations" con el cuerpo:
      """
      {
        "name": "Usuario Temporal",
        "email": "temporal@test.com",
        "description": "Invitación para borrar"
      }
      """
    Then v2 el código de estado debe ser 201
    And v2 capturo el ID de la invitación de la respuesta