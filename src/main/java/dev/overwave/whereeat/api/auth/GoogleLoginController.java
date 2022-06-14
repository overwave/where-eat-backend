package dev.overwave.whereeat.api.auth;


import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class GoogleLoginController {
    private final GoogleLoginService googleLoginService;

    @SneakyThrows
    @PostMapping("/public/login/google")
    public ResponseEntity<String> loginWithGoogleIdentity(@RequestBody LoginRequestDto loginRequest) {
        return googleLoginService.loginWithGoogle(loginRequest.token())
                .map(ResponseEntity::ok)
                .orElse(new ResponseEntity<>("invalid token", HttpStatus.UNAUTHORIZED));
    }
}
