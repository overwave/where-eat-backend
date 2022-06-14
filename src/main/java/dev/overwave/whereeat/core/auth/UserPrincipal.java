package dev.overwave.whereeat.core.auth;

import java.util.List;

public record UserPrincipal(String email, List<Role> roles,
                            String name, String familyName, String givenName, String pictureUrl) {
}
