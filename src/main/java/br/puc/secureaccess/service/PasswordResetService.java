package br.puc.secureaccess.service;

import br.puc.secureaccess.model.AppUser;
import br.puc.secureaccess.model.PasswordResetToken;
import br.puc.secureaccess.repository.AppUserRepository;
import br.puc.secureaccess.repository.PasswordResetTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

@Service
public class PasswordResetService {

    private static final int VALIDITY_MINUTES = 30;

    private final AppUserRepository users;
    private final PasswordResetTokenRepository tokens;
    private final PasswordEncoder encoder;
    private final EmailService email;
    private final String baseUrl;
    private final SecureRandom random = new SecureRandom();

    public PasswordResetService(AppUserRepository users, PasswordResetTokenRepository tokens,
                                PasswordEncoder encoder, EmailService email,
                                @Value("${app.base-url:http://localhost:8080}") String baseUrl) {
        this.users = users;
        this.tokens = tokens;
        this.encoder = encoder;
        this.email = email;
        this.baseUrl = baseUrl;
    }

    @Transactional
    public void requestReset(String emailAddress) {
        Optional<AppUser> found = users.findByEmailIgnoreCase(emailAddress.trim());
        if (!found.isPresent()) {
            return;
        }
        AppUser user = found.get();
        tokens.deleteByUser(user);

        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        tokens.save(new PasswordResetToken(hash(token), user, LocalDateTime.now().plusMinutes(VALIDITY_MINUTES)));

        email.sendPasswordReset(user.getEmail(), baseUrl + "/resetpassword?token=" + token);
    }

    @Transactional(readOnly = true)
    public boolean isValid(String token) {
        return token != null && tokens.findByTokenHash(hash(token)).filter(t -> !t.isExpired()).isPresent();
    }

    @Transactional
    public boolean resetPassword(String token, String newPassword) {
        Optional<PasswordResetToken> found = token == null
                ? Optional.<PasswordResetToken>empty()
                : tokens.findByTokenHash(hash(token));
        if (!found.isPresent() || found.get().isExpired()) {
            return false;
        }
        AppUser user = found.get().getUser();
        user.setPassword(encoder.encode(newPassword));
        users.save(user);
        tokens.deleteByUser(user);
        return true;
    }

    private static String hash(String token) {
        try {
            byte[] d = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : d) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
