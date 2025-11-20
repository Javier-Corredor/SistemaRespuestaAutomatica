package co.edu.uptc.RespuestaAutomatica.Service;

import java.util.List;

import co.edu.uptc.RespuestaAutomatica.Entities.UserEntity;

public interface UserService {
    public abstract List<UserEntity> getListUsers();
    public abstract UserEntity saveUser(Integer id, UserEntity user);
    public abstract UserEntity updateUser(Integer id, UserEntity user);
    public abstract boolean deleteUser(Integer id);
    public abstract UserEntity getUserByUsername(String username);
}