package co.edu.uptc.RespuestaAutomatica.Service.ServiceImplementation;

import co.edu.uptc.RespuestaAutomatica.Entities.UserEntity;
import co.edu.uptc.RespuestaAutomatica.Management.UserManagement;
import co.edu.uptc.RespuestaAutomatica.Service.UserService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Service("ServiciosUsuarios")
@Transactional
public class UserServiceImplementation implements UserService {

    @Autowired
    @Qualifier("CrudUsers")
    private UserManagement um;

    @Cacheable("users")
    public List<UserEntity> getListUsers() {
        System.out.println("Consultando base de datos...");
        return (List<UserEntity>) um.findAll();
    }

    @Override
    @CachePut("GuardarUser")
    public UserEntity saveUser(int id, UserEntity user) {
        return um.save(user);
    }

    @Cacheable("BuscarUser")
    public UserEntity findById(int id) {
        return um.findById(id).orElse(null);
    }

    @Override
    @CachePut("ActualizarUser")
    public UserEntity updateUser(int id, UserEntity user) {
        return um.save(user);
    }

    @Override
    @CacheEvict("EliminarUser")
    public boolean deleteUser(int id) {
        um.deleteById(id);
        return true;
    }

}
