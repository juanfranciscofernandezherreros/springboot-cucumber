@admin @management @full_crud
Feature: Gestión Extendida de Usuarios para Administradores

  Background: Autenticación de Super Admin
    Given v2 envío una petición POST a "/auth/login" con el cuerpo:
      """
      {
        "email": "admin@test.com",
        "password": "admin123"
      }
      """
    Then v2 el código de estado debe ser 200
    And v2 la respuesta debe contener un token JWT

  Scenario: Ciclo de gestión: Bloquear, Desbloquear y Cambiar Rol
    # 1. Bloquear usuario
    When v2 envío una petición POST a "/api/v1/admin/lock-user/user2@test.com" con autorización
    Then v2 el código de estado debe ser 200
    And v2 la respuesta contiene "bloqueado correctamente"

    # 2. Verificar que aparece en la lista de bloqueados
    When v2 envío una petición GET a "/api/v1/admin/locked-users" con autorización
    Then v2 el código de estado debe ser 200
    And v2 la respuesta debe ser una lista que contiene el email "user2@test.com"

    # 3. Desbloquear usuario
    When v2 envío una petición POST a "/api/v1/admin/unlock/user2@test.com" con autorización
    Then v2 el código de estado debe ser 200
    And v2 la respuesta contiene "desbloqueado correctamente"

    # 4. Actualizar Rol de Usuario
    When v2 envío una petición PUT a "/api/v1/admin/update-role" con el cuerpo:
      """
      {
        "email": "user2@test.com",
        "role": "MANAGER"
      }
      """
    Then v2 el código de estado debe ser 200
    And v2 la respuesta contiene "actualizado a MANAGER"

  Scenario: Actualizar datos de usuario y eliminar
    # 1. Obtenemos el ID de user2 para poder borrarlo/editarlo
    When v2 envío una petición GET a "/api/v1/admin/user-status?email=user3@test.com" con autorización
    And v2 capturo el ID de la invitación de la respuesta

    # 2. Editar usuario por ID
    When v2 envío una petición PUT a "/api/v1/admin/update-user/{id}" con el cuerpo:
      """
      {
        "name": "Nombre Editado Admin",
        "email": "user3@test.com"
      }
      """
    Then v2 el código de estado debe ser 200

    # 3. Eliminar usuario
    When v2 envío una petición DELETE para el usuario con ID capturado
    Then v2 el código de estado debe ser 200
    And v2 la respuesta contiene "eliminado correctamente"

  Scenario: Crear un nuevo usuario desde el panel de administración
    # POST /api/v1/admin/create-user
    When v2 envío una petición POST a "/api/v1/admin/create-user" con el cuerpo:
      """
      {
        "name": "Nuevo Usuario Admin",
        "email": "admin_created_new@test.com",
        "password": "password123",
        "role": "USER"
      }
      """
    # Validamos 201 porque el controlador devuelve HttpStatus.CREATED
    Then v2 el código de estado debe ser 201

  Scenario: Listar todos los usuarios del sistema
    # GET /api/v1/admin/users
    When v2 envío una petición GET a "/api/v1/admin/users" con autorización
    Then v2 el código de estado debe ser 200
    And v2 la respuesta debe ser una lista con elementos
    And v2 la respuesta debe ser una lista que contiene el email "admin_created@test.com"

  @stats
  Scenario: Obtener estadísticas globales de usuarios
    # GET /api/v1/admin/stats
    When v2 envío una petición GET a "/api/v1/admin/stats" con autorización
    Then v2 el código de estado debe ser 200
