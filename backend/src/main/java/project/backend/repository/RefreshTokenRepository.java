package project.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import project.backend.entity.RefreshToken;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findBtToken(String token);

    void deleteByToken(UUID userId);
}
