package dev.overwave.whereeat.core.auth;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
@AllArgsConstructor
public class UserService {

    private final SessionRepository sessionRepository;

    public Optional<UserPrincipal> getUserByToken(String token) {
        return sessionRepository.findSessionByToken(token)
                .map(Session::getUser)
                .map(UserPrincipal::map);
    }
}