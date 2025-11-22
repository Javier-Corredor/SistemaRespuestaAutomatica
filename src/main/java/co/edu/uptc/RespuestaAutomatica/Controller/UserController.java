package co.edu.uptc.RespuestaAutomatica.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import co.edu.uptc.RespuestaAutomatica.Service.ServiceImplementation.UserServiceImplementation;
import co.edu.uptc.RespuestaAutomatica.Entities.UserEntity;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "*")
@Tag(name = "User Controller", description = "CRUD de usuarios")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    @Autowired
	@Qualifier("ServiciosUsuarios")
	private UserServiceImplementation esi;

    @Operation(summary = "Listar todos los Usuarios", description = "Devuelve una lista con todos los usuarios creados y almacenados en la base de datos.")
	@GetMapping(path = "/listar/", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<UserEntity> listarUsuarios() {
		logger.info("El usuario accedio al servicio listar Users");
		return esi.getListUsers();
	}

	@Operation(summary = "Guarda al Usuario ingresado", description = "Al ingresar un usuario nuevo, este se guarda en la base de datos.\nEl campo 'email' puede enviarse como la parte local (sin '@') o con '@uptc.edu.co'.\nLa parte local permite letras, dígitos, '.', '_' y '-', longitud 1-64; no puede comenzar/terminar con '.' ni contener '..'.")
	@PostMapping(path = "/guardar/", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public UserEntity guardarUsuario(@RequestBody UserEntity user) {
		logger.info("El usuario accedio al servicio guardar Users");
		return esi.saveUser(user.getId(), user);
	}

	@Operation(summary = "Actualiza al Usuario", description = "Al ingresar el identificador de un usuario, este se actualiza en la base de datos.")
	@PostMapping(path = "/actualizar/", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public UserEntity actualizarUsuario(@RequestBody UserEntity User) {
		logger.info("El usuario accedio al servicio actualizar Users");
		return esi.updateUser(User.getId(), User);
	}

	@Operation(summary = "Eliminar al Usuario", description = "Al ingresar el identificador de un usuario, este se elimina de la base de datos.")
	@DeleteMapping(path = "/eliminar/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public boolean eliminarUsuario(@PathVariable int id) {
		logger.info("El usuario accedio al servicio eliminar Users");
		return esi.deleteUser(id);
	}
}