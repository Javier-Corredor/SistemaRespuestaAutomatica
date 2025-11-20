package co.edu.uptc.RespuestaAutomatica.Config;

import co.edu.uptc.RespuestaAutomatica.Entities.UserEntity;
import co.edu.uptc.RespuestaAutomatica.Management.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminDataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(AdminDataInitializer.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        try {
            String adminUsername = "admin";
            if (userRepository.findByUsername(adminUsername) == null) {
                UserEntity admin = new UserEntity();
                admin.setUsername(adminUsername);
                admin.setEmail("admin@local.local");
                admin.setPassword(passwordEncoder.encode("Admin123!"));
                admin.setRole("ADMIN");

                UserEntity saved = userRepository.save(admin);
                logger.info("Usuario admin creado por defecto: {} (id={})", adminUsername, saved.getId());
            } else {
                logger.info("Usuario admin ya existe, no se crea uno por defecto");
            }
        } catch (Exception e) {
            logger.error("Error creando usuario admin por defecto: ", e);
        }
    }
}
