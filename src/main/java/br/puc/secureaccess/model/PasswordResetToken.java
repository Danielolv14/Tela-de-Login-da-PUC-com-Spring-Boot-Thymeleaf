package br.puc.secureaccess.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String tokenHash;

    @ManyToOne(optional = false)
    private AppUser user;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    protected PasswordResetToken() {
    }

    public PasswordResetToken(String tokenHash, AppUser user, LocalDateTime expiresAt) {
        this.tokenHash = tokenHash;
        this.user = user;
        this.expiresAt = expiresAt;
    }

    public AppUser getUser() { return user; }
    public boolean isExpired() { return LocalDateTime.now().isAfter(expiresAt); }
}
