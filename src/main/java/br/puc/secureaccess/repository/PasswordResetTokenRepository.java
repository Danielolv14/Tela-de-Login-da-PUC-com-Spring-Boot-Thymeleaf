package br.puc.secureaccess.repository;

import br.puc.secureaccess.model.AppUser;
import br.puc.secureaccess.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    void deleteByUser(AppUser user);
}
