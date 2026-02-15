@admin @management @full_crud
Feature: Gestión Extendida de Usuarios para Administradores

  Background: Autenticación de Super Admin
    Given v2 envío una petición POST a "/auth/login" con el cuerpo:
      """
      {
        "email": "admin_mgmt_lock_read_update@test.com",
        "password": "admin123"
      }
      """
    Then v2 el código de estado debe ser 200
    And v2 la respuesta debe contener un token JWT

  # =============================================================
  # CICLO DE VIDA Y ESTADOS
  # =============================================================

  Scenario: 01 - Ciclo de gestión: Bloquear, Desbloquear y Cambiar Rol
    # 1. Bloquear usuario
    When v2 envío una petición POST a "/api/v1/admin/lock-user/user2@test.com" con autorización
    Then v2 el código de estado debe ser 200
    And v2 la respuesta contiene "bloqueado correctamente"

    # 2. Verificar que aparece en la lista de bloqueados (GET directo, sin wrapper)
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

  # =============================================================
  # MODIFICACIÓN Y ELIMINACIÓN
  # =============================================================

  Scenario: 02 - Actualizar datos de usuario y eliminar
    # Re-login con admin_read para obtener usuario
    Given v2 envío una petición POST a "/auth/login" con el cuerpo:
      """
      {
        "email": "admin_read@test.com",
        "password": "admin123"
      }
      """
    Then v2 el código de estado debe ser 200

    # 1. Obtenemos el ID de user3 (GET directo)
    When v2 envío una petición GET a "/api/v1/admin/user-status?email=user3@test.com" con autorización
    And v2 capturo el ID de la invitación de la respuesta

    # Re-login con admin_update para editar
    Given v2 envío una petición POST a "/auth/login" con el cuerpo:
      """
      {
        "email": "admin_update@test.com",
        "password": "admin123"
      }
      """
    Then v2 el código de estado debe ser 200

    # 2. Editar usuario por ID (Devuelve AdminActionResponse)
    When v2 envío una petición PUT a "/api/v1/admin/update-user/{id}" con el cuerpo:
      """
      {
        "name": "Nombre Editado Admin",
        "email": "user3@test.com"
      }
      """
    Then v2 el código de estado debe ser 200

    # Re-login con admin_delete para eliminar
    Given v2 envío una petición POST a "/auth/login" con el cuerpo:
      """
      {
        "email": "admin_delete@test.com",
        "password": "admin123"
      }
      """
    Then v2 el código de estado debe ser 200

    # 3. Eliminar usuario
    When v2 envío una petición DELETE para el usuario con ID capturado
    Then v2 el código de estado debe ser 200
    And v2 la respuesta contiene "eliminado correctamente"

  # =============================================================
  # CREACIÓN Y ERRORES
  # =============================================================

  Scenario: 03 - Crear un nuevo usuario desde el panel de administración
    Given v2 envío una petición POST a "/auth/login" con el cuerpo:
      """
      {
        "email": "admin_create@test.com",
        "password": "admin123"
      }
      """
    Then v2 el código de estado debe ser 200

    # POST /api/v1/admin/create-user (Devuelve AdminActionResponse + 201)
    When v2 envío una petición POST a "/api/v1/admin/create-user" con el cuerpo:
      """
      {
        "name": "Nuevo Usuario Admin",
        "email": "admin_created_new@test.com",
        "password": "password123",
        "role": "USER"
      }
      """
    Then v2 el código de estado debe ser 201
    And v2 la respuesta contiene "admin_created_new@test.com"

  Scenario: 04 - Intentar crear un usuario que ya existe desde el panel de administración
    Given v2 envío una petición POST a "/auth/login" con el cuerpo:
      """
      {
        "email": "admin_create@test.com",
        "password": "admin123"
      }
      """
    Then v2 el código de estado debe ser 200

    # Debe lanzar la UserAlreadyExistsException (400 Bad Request)
    When v2 envío una petición POST a "/api/v1/admin/create-user" con el cuerpo:
      """
      {
        "name": "Usuario Duplicado",
        "email": "user2@test.com",
        "password": "password123",
        "role": "USER"
      }
      """
    Then v2 el código de estado debe ser 400

  # =============================================================
  # CONSULTAS GLOBALES
  # =============================================================

  Scenario: 05 - Listar todos los usuarios del sistema
    Given v2 envío una petición POST a "/auth/login" con el cuerpo:
      """
      {
        "email": "admin_read@test.com",
        "password": "admin123"
      }
      """
    Then v2 el código de estado debe ser 200

    When v2 envío una petición GET a "/api/v1/admin/users" con autorización
    Then v2 el código de estado debe ser 200
    And v2 la respuesta debe ser una lista con elementos

  Scenario: 06 - Obtener estadísticas globales de usuarios
    Given v2 envío una petición POST a "/auth/login" con el cuerpo:
      """
      {
        "email": "admin_read@test.com",
        "password": "admin123"
      }
      """
    Then v2 el código de estado debe ser 200

    When v2 envío una petición GET a "/api/v1/admin/stats" con autorización
    Then v2 el código de estado debe ser 200