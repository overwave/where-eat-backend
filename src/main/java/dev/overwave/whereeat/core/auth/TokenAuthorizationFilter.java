package dev.overwave.whereeat.core.auth;

import javax.annotation.Nonnull;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class TokenAuthorizationFilter extends OncePerRequestFilter {

    private final UserService userService;

    @Override
    @SneakyThrows
    protected void doFilterInternal(
            @Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull FilterChain chain) {
        String token = request.getHeader(HttpHeaders.AUTHORIZATION);
        Optional<UserPrincipal> userO = userService.getUserByToken(token);
        if (userO.isPresent()) {
            UserPrincipal user = userO.get();
            List<SimpleGrantedAuthority> roles = user.roles().stream()
                    .map(Enum::name)
                    .map(SimpleGrantedAuthority::new)
                    .toList();
            Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(user, token, roles);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        chain.doFilter(request, response);
    }
}