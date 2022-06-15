package dev.overwave.whereeat.core.auth;

import java.util.Collections;
import java.util.List;

public record UserPrincipal(String email, List<Role> roles,
                            String name, String familyName, String givenName, String pictureUrl) {
    public static UserPrincipal map(User user) {
        return new UserPrincipal(user.getEmail(), Collections.unmodifiableList(user.getRoles()), user.getName(),
                user.getFamilyName(), user.getGivenName(), user.getPictureUrl());
    }
}
