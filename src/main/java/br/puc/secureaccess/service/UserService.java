package br.puc.secureaccess.service;

import br.puc.secureaccess.dto.RegisterForm;
import br.puc.secureaccess.model.AppUser;
import br.puc.secureaccess.repository.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final AppUserRepository users;
    private final PasswordEncoder encoder;

    public UserService(AppUserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    public boolean usernameTaken(String username) {
        return users.existsByUsernameIgnoreCase(username.trim());
    }

    public boolean emailTaken(String email) {
        return users.existsByEmailIgnoreCase(email.trim());
    }

    public AppUser register(RegisterForm form) {
        return users.save(new AppUser(form.getUsername().trim(), form.getEmail().trim().toLowerCase(),
                encoder.encode(form.getPassword())));
    }
}
