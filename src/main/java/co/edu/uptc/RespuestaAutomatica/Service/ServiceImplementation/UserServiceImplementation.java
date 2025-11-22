package co.edu.uptc.RespuestaAutomatica.Service.ServiceImplementation;

import co.edu.uptc.RespuestaAutomatica.Entities.UserEntity;
import co.edu.uptc.RespuestaAutomatica.Management.UserManagement;
import co.edu.uptc.RespuestaAutomatica.Management.UserRepository;
import co.edu.uptc.RespuestaAutomatica.Service.UserService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Service("ServiciosUsuarios")
@Transactional
public class UserServiceImplementation implements UserService {

    @Autowired
    @Qualifier("CrudUsers")
    private UserManagement um;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Cacheable("users")
    public List<UserEntity> getListUsers() {
        System.out.println("Consultando base de datos...");
        return (List<UserEntity>) um.findAll();
    }

    @Override
    @CachePut("GuardarUser")
    public UserEntity saveUser(Integer id, UserEntity user) {
        // Normalizar email: si viene sin '@', agregar dominio por defecto
        final String DOMAIN = "@uptc.edu.co";
        String email = user.getEmail();
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("El email es obligatorio");
        }
        email = email.trim();
        if (!email.contains("@")) {
            // si viene solo la parte local, la usamos y validamos
            String local = email;
            validateEmailLocalPart(local);
            email = local + DOMAIN;
        } else {
            String[] parts = email.split("@", 2);
            if (parts.length < 2 || parts[1] == null || parts[1].isBlank()) {
                validateEmailLocalPart(parts[0]);
                email = parts[0] + DOMAIN;
            } else if (!parts[1].equalsIgnoreCase(DOMAIN.substring(1))) {
                throw new IllegalArgumentException("El dominio del email debe ser '@uptc.edu.co'");
            } else {
                // valida la parte local si viene con dominio correcto
                validateEmailLocalPart(parts[0]);
            }
        }
        user.setEmail(email.toLowerCase());

        // Validar duplicado de email
        UserEntity existing = userRepository.findByEmail(user.getEmail());
        if (existing != null && (id == null || !existing.getId().equals(id))) {
            throw new IllegalArgumentException("Ya existe un usuario con ese email");
        }

        if (user.getPassword() != null && !user.getPassword().startsWith("$2a$")) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        if (user.getRole() == null || user.getRole().isBlank()) {
            user.setRole("USER");
        }
        return um.save(user);
    }

    @Cacheable("BuscarUser")
    public UserEntity findById(Integer id) {
        return um.findById(id).orElse(null);
    }

    @Override
    @CachePut("ActualizarUser")
    public UserEntity updateUser(Integer id, UserEntity user) {
        if (user.getPassword() != null && !user.getPassword().startsWith("$2a$")) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        if (user.getRole() == null || user.getRole().isBlank()) {
            user.setRole("USER");
        }
        return um.save(user);
    }

    @Override
    @CacheEvict("EliminarUser")
    public boolean deleteUser(Integer id) {
        um.deleteById(id);
        return true;
    }

    @Override
    public UserEntity getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public UserEntity getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // Valida la parte local del email (antes de @)
    private void validateEmailLocalPart(String local) {
        if (local == null) {
            throw new IllegalArgumentException("La parte local del email es obligatoria");
        }
        local = local.trim();
        // Longitud: de 1 a 64 caracteres (RFC)
        if (local.isEmpty() || local.length() > 64) {
            throw new IllegalArgumentException("La parte local del email debe tener entre 1 y 64 caracteres");
        }
        // No puede comenzar ni terminar con '.'
        if (local.startsWith(".") || local.endsWith(".")) {
            throw new IllegalArgumentException("La parte local del email no puede comenzar ni terminar con '.'");
        }
        // No se permiten dos puntos consecutivos
        if (local.contains("..")) {
            throw new IllegalArgumentException("La parte local del email no puede contener '..' consecutivos");
        }
        // Permitir letras, dígitos, punto, guion y guion bajo
        if (!local.matches("[A-Za-z0-9._-]+")) {
            throw new IllegalArgumentException("La parte local del email contiene caracteres inválidos. Permitidos: letras, dígitos, '.', '_' y '-'");
        }
    }

}