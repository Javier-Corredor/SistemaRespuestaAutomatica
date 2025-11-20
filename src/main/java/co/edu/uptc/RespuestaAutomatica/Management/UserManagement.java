package co.edu.uptc.RespuestaAutomatica.Management;

import co.edu.uptc.RespuestaAutomatica.Entities.UserEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository ("CrudUsers")
public interface UserManagement extends CrudRepository<UserEntity, Integer> {
    
}