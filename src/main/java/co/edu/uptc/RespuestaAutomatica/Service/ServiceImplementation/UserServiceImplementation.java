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

}