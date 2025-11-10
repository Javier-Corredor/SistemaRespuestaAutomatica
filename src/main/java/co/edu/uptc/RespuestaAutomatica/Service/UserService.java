package co.edu.uptc.RespuestaAutomatica.Service;

import java.util.List;

import co.edu.uptc.RespuestaAutomatica.Entities.UserEntity;

public interface UserService {
    public abstract List<UserEntity> getListUsers();
    public abstract UserEntity saveUser(int id, UserEntity user);
    public abstract UserEntity updateUser(int id, UserEntity user);
    public abstract boolean deleteUser(int id);
}
