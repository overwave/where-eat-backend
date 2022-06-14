package dev.overwave.whereeat.core.auth;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;


@Service
@AllArgsConstructor
public class UserService {

    private final SessionRepository sessionRepository;

    public Optional<UserPrincipal> getUserByToken(String token) {
        return sessionRepository.findSessionByToken(token)
                .map(Session::getUser)
                .map(UserService::toUserPrincipal);
    }

    private static UserPrincipal toUserPrincipal(User user) {
        return new UserPrincipal(user.getEmail(), Collections.unmodifiableList(user.getRoles()), user.getName(),
                user.getFamilyName(), user.getGivenName(), user.getPictureUrl());
    }
}