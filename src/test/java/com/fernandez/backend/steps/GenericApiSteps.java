package com.fernandez.backend.steps;

import io.cucumber.java.PendingException;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.serenitybdd.core.Serenity;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

public class GenericApiSteps {

    private HttpResponse<String> response;

    // Variables estáticas para compartir tokens entre escenarios
    private static String accessToken;
    private static String refreshToken;
    private static Long currentInvitationId;

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private final String BASE_URL = "http://localhost:8087";

    // --- STEPS DE ENVÍO ---
    @When("v2 envío una petición POST para cambiar mi contraseña con el cuerpo:")
    public void v2EnvioCambioPassword(String jsonBody) throws Exception {
        String endpoint = "/api/v1/users/me/password";
        String fullUrl = BASE_URL + endpoint;

        // Validamos que estemos autenticados
        assertNotNull(accessToken, "Error: No hay un token de acceso disponible. Debes iniciar sesión primero.");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(fullUrl))
                .header("Content-Type", "application/json")
                .header("accept", "*/*")
                .header("Authorization", "Bearer " + accessToken) // Inyección del token del curl
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        this.response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Si el cambio es exitoso, el server devuelve nuevos tokens. Los extraemos.
        if (response.statusCode() == 200) {
            extractTokens(response.body());
            System.out.println("✅ Contraseña cambiada y nuevos tokens capturados.");
        }

        registrarEnSerenity("POST " + endpoint, jsonBody, response.body());
    }

    @When("v2 envío una petición POST a {string} con el cuerpo:")
    public void v2EnvioPeticionPostDocString(String endpoint, String jsonBody) throws Exception {
        ejecutarPeticionPost(2, endpoint, jsonBody);
    }

    @And("v{int} envío una petición POST a {string} con el cuerpo: {string}")
    public void vEnvioPeticionPostInline(int version, String endpoint, String jsonBody) throws Exception {
        ejecutarPeticionPost(version, endpoint, jsonBody);
    }

    @And("v2 envío una petición POST para cerrar sesión a {string}")
    public void v2EnvioLogout(String endpoint) throws Exception {
        assertNotNull(accessToken, "No hay access_token disponible para logout");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Authorization", "Bearer " + accessToken)
                .header("accept", "*/*")
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        this.response = client.send(request, HttpResponse.BodyHandlers.ofString());
        registrarEnSerenity("Logout POST " + endpoint, "Token: " + accessToken, response.body());
    }

    // --- STEPS DE VALIDACIÓN ---

    @Then("v2 el código de estado debe ser {int}")
    public void v2ValidarStatus(int expectedStatusCode) {
        assertNotNull(response, "No se recibió respuesta del servidor");
        assertEquals(expectedStatusCode, response.statusCode(), "El código de estado HTTP no coincide");
    }

    @Then("v2 la respuesta debe contener un token JWT")
    public void v2ValidarJwt() {
        assertNotNull(accessToken, "El access_token es nulo");
        assertTrue(accessToken.length() > 20 && accessToken.contains("."), "El token no parece un JWT válido");
    }

    @When("v2 envío una petición GET a {string} con autorización")
    public void v2EnvioPeticionGetAutorizada(String endpoint) throws Exception {
        String fullUrl = BASE_URL + endpoint;

        // Validamos que tengamos un token (obtenido en un login previo)
        assertNotNull(accessToken, "Error: Se requiere un token de acceso. ¿Te has logueado como ADMIN?");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(fullUrl))
                .header("Authorization", "Bearer " + accessToken)
                .header("accept", "*/*")
                .GET() // Petición GET
                .build();

        try {
            this.response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Registro en reporte Serenity
            Serenity.recordReportData().withTitle("GET Request: " + endpoint)
                    .andContents("URL: " + fullUrl + "\nToken: " + accessToken.substring(0, 10) + "...");
            Serenity.recordReportData().withTitle("GET Response: " + endpoint)
                    .andContents("Status: " + response.statusCode() + "\nBody: " + response.body());

        } catch (Exception e) {
            Serenity.recordReportData().withTitle("Connection Error GET").andContents(e.getMessage());
            throw e;
        }
    }

    @And("v2 la respuesta contiene {string}")
    public void v2LaRespuestaContiene(String textoEsperado) {
        assertNotNull(response, "No se recibió respuesta del servidor para validar el texto");
        String cuerpoRespuesta = response.body();

        // Validamos que el texto esté presente
        assertTrue(cuerpoRespuesta.contains(textoEsperado),
                String.format("❌ Error: Se esperaba encontrar '%s' en la respuesta, pero se obtuvo: %s",
                        textoEsperado, cuerpoRespuesta));

        // Registro en el reporte de Serenity para trazabilidad
        Serenity.recordReportData()
                .withTitle("Validación de Mensaje")
                .andContents("Texto buscado: " + textoEsperado);
    }

    @When("v2 envío una petición PUT para actualizar mi perfil con el cuerpo:")
    public void v2EnvioUpdateProfile(String jsonBody) throws Exception {
        String fullUrl = BASE_URL + "/api/v1/users/update";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(fullUrl))
                .header("Authorization", "Bearer " + accessToken) // <--- ESTO ES VITAL
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        this.response = client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    // --- LÓGICA PRIVADA REUTILIZABLE ---

    private void ejecutarPeticionPost(int version, String endpoint, String jsonBody) throws Exception {
        String fullUrl = BASE_URL + endpoint;
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(fullUrl))
                .header("Content-Type", "application/json")
                .header("accept", "*/*");

        // 1. CASO REFRESH TOKEN: Se inyecta el refreshToken en el Header
        if (endpoint.contains("/auth/refresh-token")) {
            if (GenericApiSteps.refreshToken == null) {
                throw new IllegalStateException("❌ ERROR v3: Intentaste refrescar token pero 'refreshToken' es NULL.");
            }
            builder.header("Authorization", "Bearer " + GenericApiSteps.refreshToken.replace("\"", ""));
            System.out.println("🔄 [v3-DEBUG] Inyectando Refresh Token en Header.");
        }
        // 2. CASO RUTAS PROTEGIDAS: Se inyecta el accessToken (si no es login/register)
        else if (GenericApiSteps.accessToken != null && !endpoint.contains("/auth/")) {
            builder.header("Authorization", "Bearer " + GenericApiSteps.accessToken.replace("\"", ""));
        }

        HttpRequest request = builder.POST(HttpRequest.BodyPublishers.ofString(jsonBody)).build();

        try {
            this.response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // CRÍTICO: Extraer siempre que la respuesta sea exitosa (200 o 201)
            if (response.statusCode() == 200 || response.statusCode() == 201) {
                extractTokens(response.body());
            }

            registrarEnSerenity("POST v" + version + " " + endpoint, jsonBody, response.body());
        } catch (Exception e) {
            System.err.println("❌ ERROR en petición POST: " + e.getMessage());
            throw e;
        }
    }

    private void extractTokens(String body) {
        // Imprimimos el body para que tú mismo lo veas en la consola si falla
        System.out.println("🔍 [DEBUG LOGIN BODY]: " + body);

        // Regex mejorado: Busca "access_token" o "accessToken", con o sin espacios
        Pattern accessPattern = Pattern.compile("\"access_?token\"\\s*:\\s*\"([^\"]+)\"");
        Pattern refreshPattern = Pattern.compile("\"refresh_?token\"\\s*:\\s*\"([^\"]+)\"");

        Matcher accessMatcher = accessPattern.matcher(body);
        Matcher refreshMatcher = refreshPattern.matcher(body);

        if (accessMatcher.find()) {
            GenericApiSteps.accessToken = accessMatcher.group(1);
            System.out.println("✅ [v3] Access Token persistido: " + GenericApiSteps.accessToken.substring(0, 10) + "...");
        }

        if (refreshMatcher.find()) {
            GenericApiSteps.refreshToken = refreshMatcher.group(1);
        }

        // Si sigue siendo null, intentamos una captura bruta por si el JSON es simple
        if (GenericApiSteps.accessToken == null && body.contains(".")) {
            // Si el body es solo el token o tiene formato JWT, lo asignamos
            if(body.split("\\.").length == 3) GenericApiSteps.accessToken = body.replace("\"", "");
        }
    }

    private String getValueFromJson(String json, String key) {
        Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*\"?([^\",\\s}]+)\"?");
        Matcher matcher = pattern.matcher(json);
        return matcher.find() ? matcher.group(1) : null;
    }

    private void registrarEnSerenity(String titulo, String request, String responseBody) {
        Serenity.recordReportData().withTitle(titulo + " - Request").andContents(request);
        Serenity.recordReportData().withTitle(titulo + " - Response").andContents(responseBody);
    }

    @Then("v2 la respuesta debe ser una lista de logs de auditoría")
    public void v2ValidarListaAuditoria() {
        String body = response.body();
        assertNotNull(body, "El cuerpo de la respuesta está vacío");
        // Verificamos que empiece por '[' (un array JSON)
        assertTrue(body.trim().startsWith("["), "La respuesta no es una lista JSON: " + body);

        // Log para ver cuántos registros hay
        System.out.println("DEBUG: Se han recuperado registros de auditoría.");
    }

    @And("v2 la respuesta debe ser una lista con elementos")
    public void v2ValidarListaConElementos() {
        String body = response.body();
        assertNotNull(body, "El cuerpo de la respuesta es nulo");

        // Verificamos que sea un array JSON y que no esté vacío "[]"
        assertTrue(body.trim().startsWith("[") && body.trim().endsWith("]"),
                "La respuesta no es un array JSON válido");

        // Eliminamos espacios y comprobamos si es exactamente "[]"
        assertFalse(body.trim().equals("[]"), "La lista de resultados está vacía");

        // Log para el reporte de Serenity con el conteo aproximado (contando comas + 1 si no es [])
        int count = body.equals("[]") ? 0 : body.split("\\{\"id\"").length - 1;
        System.out.println("DEBUG: Se han encontrado " + count + " elementos en la lista.");
        Serenity.recordReportData().withTitle("Elementos encontrados").andContents("Total: " + count);
    }

    @When("v2 envío una petición PUT a {string} con el cuerpo:")
    public void v2EnvioPutGeneral(String endpoint, String jsonBody) throws Exception {
        // 1. Lógica de ID: Solo si la URL contiene {id} lo reemplazamos
        String finalEndpoint = endpoint;
        if (endpoint.contains("{id}")) {
            assertNotNull(currentInvitationId, "Error: La URL requiere un {id} pero no se ha capturado ninguno previamente.");
            finalEndpoint = endpoint.replace("{id}", String.valueOf(currentInvitationId));
        }

        String fullUrl = BASE_URL + finalEndpoint;

        // 2. Lógica de Token: Validamos que exista para evitar el 401
        assertNotNull(accessToken, "Error: El accessToken es null. ¿Olvidaste hacer login en el Background?");

        // 3. Construcción de la petición
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(fullUrl))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .header("accept", "*/*")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        try {
            this.response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Registro en Serenity
            registrarEnSerenity("PUT " + finalEndpoint, jsonBody, response.body());

            // Si la respuesta es exitosa y hay un ID nuevo en el JSON, lo capturamos (opcional)
            if (response.statusCode() == 200 && response.body().contains("\"id\":")) {
                String idCapturado = getValueFromJson(response.body(), "id");
                if (idCapturado != null) currentInvitationId = Long.parseLong(idCapturado);
            }
        } catch (Exception e) {
            throw e;
        }
    }

    @And("v2 la respuesta debe contener el nombre {string}")
    public void v2ValidarNombreEnRespuesta(String nombreEsperado) {
        assertTrue(response.body().contains(nombreEsperado),
                "La respuesta no contiene el nombre: " + nombreEsperado);
    }

    @And("v{int} capturo el ID de la invitación de la respuesta")
    public void vCapturoElIDDeLaInvitaciónDeLaRespuesta(int version) {
        String body = response.body();

        // Usamos el método auxiliar getValueFromJson que ya tenemos
        // para capturar el valor de la clave "id"
        String idCapturado = getValueFromJson(body, "id");

        if (idCapturado != null) {
            currentInvitationId = Long.parseLong(idCapturado);

            // Log para debug en consola y reporte
            System.out.println("✅ ID de invitación capturado: " + currentInvitationId);
            Serenity.recordReportData()
                    .withTitle("ID Capturado (v" + version + ")")
                    .andContents("ID: " + currentInvitationId);
        } else {
            fail("No se pudo encontrar el campo 'id' en la respuesta: " + body);
        }
    }

    @When("v2 envío una petición DELETE para el usuario con ID capturado")
    public void v2EnvioDeleteUsuarioCapturado() throws Exception {
        // Validamos que el ID no sea nulo (capturado en un step previo)
        assertNotNull(currentInvitationId, "Error: No se ha capturado ningún ID de usuario previamente.");

        String endpoint = "/api/v1/admin/delete/" + currentInvitationId;
        String fullUrl = BASE_URL + endpoint;

        assertNotNull(accessToken, "Error: Se requiere token de administrador para borrar usuarios.");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(fullUrl))
                .header("Authorization", "Bearer " + accessToken)
                .header("accept", "*/*")
                .DELETE() // Petición DELETE
                .build();

        try {
            this.response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Registro en Serenity para el reporte
            Serenity.recordReportData()
                    .withTitle("DELETE User Request")
                    .andContents("URL: " + fullUrl + "\nID: " + currentInvitationId);

            Serenity.recordReportData()
                    .withTitle("DELETE User Response")
                    .andContents("Status: " + response.statusCode() + "\nBody: " + response.body());

            System.out.println("✅ Usuario con ID " + currentInvitationId + " eliminado. Status: " + response.statusCode());

        } catch (Exception e) {
            Serenity.recordReportData().withTitle("Connection Error DELETE").andContents(e.getMessage());
            throw e;
        }
    }

    @When("v2 envío una petición POST a {string} con autorización")
    public void v2EnvioPostAdminAutorizado(String endpoint) throws Exception {
        String fullUrl = BASE_URL + endpoint;

        // Validamos que el token de administrador esté cargado (del login previo)
        assertNotNull(accessToken, "Error: Se requiere un token de acceso de administrador.");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(fullUrl))
                .header("Authorization", "Bearer " + accessToken)
                .header("accept", "*/*")
                .POST(HttpRequest.BodyPublishers.noBody()) // Envío sin cuerpo
                .build();

        try {
            this.response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Registro en Serenity para el reporte detallado
            Serenity.recordReportData()
                    .withTitle("POST Admin Action: " + endpoint)
                    .andContents("Status: " + response.statusCode() + "\nBody: " + response.body());

            System.out.println("DEBUG: Acción admin POST en " + endpoint + " ejecutada (Status: " + response.statusCode() + ")");
        } catch (Exception e) {
            Serenity.recordReportData().withTitle("Connection Error POST Admin").andContents(e.getMessage());
            throw e;
        }
    }

    @And("v{int} la respuesta debe ser una lista que contiene el email {string}")
    public void vLaRespuestaDebeSerUnaListaQueContieneElEmail(int version, String emailEsperado) {
        assertNotNull(response, "No hay respuesta del servidor para validar");
        String body = response.body();

        // 1. Validamos que sea una lista (empieza por [)
        assertTrue(body.trim().startsWith("["), "La respuesta no es una lista JSON: " + body);

        // 2. Validamos que el email esté presente en el JSON
        assertTrue(body.contains(emailEsperado),
                String.format("❌ Error: Se esperaba encontrar el email '%s' en la lista, pero no se encontró. Respuesta: %s",
                        emailEsperado, body));

        // Registro para el reporte de Serenity
        System.out.println("✅ Email encontrado en la lista: " + emailEsperado);
        Serenity.recordReportData()
                .withTitle("Validación de Email en Lista (v" + version + ")")
                .andContents("Email buscado: " + emailEsperado + "\nRespuesta: " + body);
    }

    @When("v3 envío una petición PUT a {string} con el cuerpo:")
    public void v3EnvioPutInteligente(String endpoint, String jsonBody) throws Exception {
        // 1. Reemplazo de ID
        String finalEndpoint = endpoint;
        if (endpoint.contains("{id}")) {
            if (GenericApiSteps.currentInvitationId == null) {
                throw new IllegalStateException("❌ ERROR v3: ID de invitación es NULL.");
            }
            finalEndpoint = endpoint.replace("{id}", String.valueOf(GenericApiSteps.currentInvitationId));
        }

        // 2. Validación de Token con "Fail-Fast"
        if (GenericApiSteps.accessToken == null) {
            // Intentamos una última extracción de emergencia si hay respuesta previa
            if (response != null && response.body().contains("access_token")) {
                extractTokens(response.body());
            }

            if (GenericApiSteps.accessToken == null) {
                throw new IllegalStateException("❌ ERROR v3: 'accessToken' sigue siendo NULL tras login. Revisa la consola de Linux para ver el body del login.");
            }
        }

        // 3. Envío
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + finalEndpoint))
                .header("Authorization", "Bearer " + GenericApiSteps.accessToken)
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        this.response = client.send(request, HttpResponse.BodyHandlers.ofString());
        registrarEnSerenity("PUT v3 " + finalEndpoint, jsonBody, response.body());
        System.out.println("🚀 [v3] PUT exitoso a " + finalEndpoint);
    }

    @When("v2 envío una petición PATCH a {string} con autorización")
    public void v3EnvioPatchInteligente(String endpoint) throws Exception {
        // 1. Resolución de ID dinámico
        String finalEndpoint = endpoint;
        if (endpoint.contains("{id}")) {
            if (GenericApiSteps.currentInvitationId == null) {
                throw new IllegalStateException("❌ ERROR v3 [PATCH]: currentInvitationId es NULL. ¿Capturaste el ID antes?");
            }
            finalEndpoint = endpoint.replace("{id}", String.valueOf(GenericApiSteps.currentInvitationId));
        }

        // 2. Validación de Token
        if (GenericApiSteps.accessToken == null) {
            throw new IllegalStateException("❌ ERROR v3 [PATCH]: accessToken es NULL. El login falló o no persistió el token.");
        }

        // 3. Ejecución
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + finalEndpoint))
                .header("Authorization", "Bearer " + GenericApiSteps.accessToken)
                .header("accept", "*/*")
                .method("PATCH", HttpRequest.BodyPublishers.noBody())
                .build();

        this.response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("🚀 [v3] PATCH a: " + finalEndpoint + " | Status: " + response.statusCode());
        registrarEnSerenity("PATCH v3 " + finalEndpoint, "Sin cuerpo", response.body());
    }

    @When("v2 envío una petición DELETE a {string} con autorización")
    public void v3EnvioDeleteInteligente(String endpoint) throws Exception {
        // 1. Resolución de ID dinámico
        String finalEndpoint = endpoint;
        if (endpoint.contains("{id}")) {
            if (GenericApiSteps.currentInvitationId == null) {
                throw new IllegalStateException("❌ ERROR v3 [DELETE]: currentInvitationId es NULL.");
            }
            finalEndpoint = endpoint.replace("{id}", String.valueOf(GenericApiSteps.currentInvitationId));
        }

        // 2. Validación de Token
        if (GenericApiSteps.accessToken == null) {
            throw new IllegalStateException("❌ ERROR v3 [DELETE]: accessToken es NULL.");
        }

        // 3. Ejecución
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + finalEndpoint))
                .header("Authorization", "Bearer " + GenericApiSteps.accessToken)
                .header("accept", "*/*")
                .DELETE()
                .build();

        this.response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("🚀 [v3] DELETE a: " + finalEndpoint + " | Status: " + response.statusCode());
        registrarEnSerenity("DELETE v3 " + finalEndpoint, "ID: " + GenericApiSteps.currentInvitationId, "Status: " + response.statusCode());
    }
}