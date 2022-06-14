package dev.overwave.whereeat.core.auth;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SessionRepository extends JpaRepository<Session, Integer> {
    Optional<Session> findSessionByToken(String token);
}
