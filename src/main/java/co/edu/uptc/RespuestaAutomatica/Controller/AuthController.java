package co.edu.uptc.RespuestaAutomatica.Controller;

import co.edu.uptc.RespuestaAutomatica.DTO.AuthResponse;
import co.edu.uptc.RespuestaAutomatica.DTO.AuthTokenPackage;
import co.edu.uptc.RespuestaAutomatica.DTO.LoginRequest;
import co.edu.uptc.RespuestaAutomatica.DTO.AdminCreateRequest;
import co.edu.uptc.RespuestaAutomatica.Entities.UserEntity;
import co.edu.uptc.RespuestaAutomatica.Service.ServiceImplementation.UserServiceImplementation;
import co.edu.uptc.RespuestaAutomatica.Security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
@Tag(name = "Auth Controller", description = "Autenticación y autorización con JWT")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserServiceImplementation userService;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Operation(summary = "Login de usuario", description = "Autentica un usuario y devuelve access token y refresh token")
    @PostMapping(path = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            logger.info("Intento de login para email: {}", loginRequest.getEmail());

            UserEntity user = userService.getUserByEmail(loginRequest.getEmail());

            if (user == null) {
                logger.warn("Usuario no encontrado (email): {}", loginRequest.getEmail());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario o contraseña incorrectos");
            }

            if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                logger.warn("Contraseña incorrecta para email: {}", loginRequest.getEmail());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario o contraseña incorrectos");
            }

            String accessToken = tokenProvider.generateAccessToken(user.getUsername(), user.getEmail(), user.getRole());
            String refreshToken = tokenProvider.generateRefreshToken(user.getUsername(), user.getEmail(), user.getRole());

            AuthTokenPackage tokenPackage = AuthTokenPackage.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .authScheme("Bearer")
                    .build();

            return ResponseEntity.ok(tokenPackage);

        } catch (Exception e) {
            logger.error("Error al procesar el login: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al procesar el login");
        }
    }

    @Operation(summary = "Refresh token", description = "Genera un nuevo access token usando el refresh token")
    @PostMapping(path = "/refresh", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> refresh(@RequestParam String refreshToken) {
        try {
            if (!tokenProvider.validateRefreshToken(refreshToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh token inválido");
            }

            String username = tokenProvider.getUsernameFromToken(refreshToken);
            String email = tokenProvider.getEmailFromToken(refreshToken);
            String role = tokenProvider.getRoleFromToken(refreshToken);

            String newAccessToken = tokenProvider.generateAccessToken(username, email, role);

            AuthTokenPackage tokenPackage = AuthTokenPackage.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(refreshToken)
                    .authScheme("Bearer")
                    .build();

            return ResponseEntity.ok(tokenPackage);

        } catch (Exception e) {
            logger.error("Error al refrescar el token: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al refrescar el token");
        }
    }

    @Operation(summary = "Validar token", description = "Valida si un token JWT es válido")
    @PostMapping(path = "/validate", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> validateToken(@RequestParam String token) {
        try {
            if (tokenProvider.validateAccessToken(token)) {
                String username = tokenProvider.getUsernameFromToken(token);
                String email = tokenProvider.getEmailFromToken(token);
                return ResponseEntity.ok(new AuthResponse(token, username, email));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token inválido");
            }
        } catch (Exception e) {
            logger.error("Error al validar el token: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al validar el token");
        }
    }

    @Operation(summary = "Crear usuario admin", description = "Solo usuarios con rol ADMIN pueden crear otros admins. Requiere token de acceso válido.")
    @PostMapping(path = "/admin/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createAdmin(@RequestBody AdminCreateRequest adminRequest) {
        try {
            logger.info("Intento de crear admin: {}", adminRequest.getUsername());

            // Validar que los datos no sean nulos
            if (adminRequest.getUsername() == null || adminRequest.getUsername().isBlank() ||
                adminRequest.getEmail() == null || adminRequest.getEmail().isBlank() ||
                adminRequest.getPassword() == null || adminRequest.getPassword().isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username, email y password son requeridos");
            }

            // Validar que el usuario no exista
            UserEntity existing = userService.getUserByUsername(adminRequest.getUsername());
            if (existing != null) {
                logger.warn("Usuario ya existe: {}", adminRequest.getUsername());
                return ResponseEntity.status(HttpStatus.CONFLICT).body("El usuario ya existe");
            }

            // Crear el nuevo admin
            UserEntity newAdmin = new UserEntity();
            newAdmin.setUsername(adminRequest.getUsername());
            newAdmin.setEmail(adminRequest.getEmail());
            newAdmin.setPassword(adminRequest.getPassword());
            newAdmin.setRole("ADMIN");

            UserEntity savedAdmin = userService.saveUser(null, newAdmin);
            logger.info("Admin creado exitosamente: {} (id={})", adminRequest.getUsername(), savedAdmin.getId());

            return ResponseEntity.status(HttpStatus.CREATED).body("Admin creado exitosamente. Username: " + savedAdmin.getUsername());

        } catch (Exception e) {
            logger.error("Error al crear admin: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al crear el admin");
        }
    }
}
