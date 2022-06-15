package dev.overwave.whereeat.api.auth;

import dev.overwave.whereeat.core.auth.UserPrincipal;

public record LoginResponseDto(UserPrincipal user, String token) {
}
