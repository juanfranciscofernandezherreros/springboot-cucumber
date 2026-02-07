@admin @invitations
Feature: Gestión completa de invitaciones

  Background: Autenticación de Admin
    Given v2 envío una petición POST a "/auth/login" con el cuerpo:
      """
      {
        "email": "admin@test.com",
        "password": "admin123"
      }
      """
    And v2 el código de estado debe ser 200

  Scenario: Ciclo de vida de una invitación (CRUD completo)
    # 1. Crear (Cubrimos createInvitation)
    When v2 envío una petición POST a "/api/v1/admin/invitations" con el cuerpo:
      """
      {
        "name": "User Test",
        "email": "test-crud@example.com",
        "description": "Prueba de cobertura"
      }
      """
    Then v2 el código de estado debe ser 201
    And v2 capturo el ID de la invitación de la respuesta

    # 2. Editar (Cubrimos updateInvitation)
    When v3 envío una petición PUT a "/api/v1/admin/invitations/{id}" con el cuerpo:
      """
      {
        "name": "User Test Editado",
        "email": "test-crud@example.com",
        "description": "Descripción actualizada"
      }
      """
    Then v2 el código de estado debe ser 200
    And v2 la respuesta contiene "User Test Editado"

    # 3. Cambiar Estado (Cubrimos updateStatus y canTransitionTo)
    When v2 envío una petición PATCH a "/api/v1/admin/invitations/{id}/status?newStatus=ACCEPTED" con autorización
    Then v2 el código de estado debe ser 200
    And v2 la respuesta contiene "Estado actualizado a ACCEPTED"

    # 4. Eliminar (Cubrimos deleteInvitation)
    When v2 envío una petición DELETE a "/api/v1/admin/invitations/{id}" con autorización
    Then v2 el código de estado debe ser 204