package dev.overwave.whereeat.api.auth;


import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import dev.overwave.whereeat.core.auth.Role;
import dev.overwave.whereeat.core.auth.Session;
import dev.overwave.whereeat.core.auth.User;
import dev.overwave.whereeat.core.auth.UserRepository;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class GoogleLoginService {
    private final GoogleIdTokenVerifier tokenVerifier;
    private final UserRepository userRepository;

    public GoogleLoginService(@Value("${whereeat.oauth2.google.client-id}") String clientId,
                              @Value("${whereeat.oauth2.google.client-secret}") String clientSecret,
                              UserRepository userRepository) {
        this.userRepository = userRepository;
        NetHttpTransport transport = new NetHttpTransport();
        GsonFactory jsonFactory = new GsonFactory();
        this.tokenVerifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                .setAudience(List.of(clientId))
                // Or, if multiple clients access the backend:
                //.setAudience(Arrays.asList(CLIENT_ID_1, CLIENT_ID_2, CLIENT_ID_3))
                .build();
    }

    @SneakyThrows
    public Optional<String> loginWithGoogle(String jwtIdToken) {
        GoogleIdToken idToken = tokenVerifier.verify(jwtIdToken);
        if (idToken == null) {
            return Optional.empty();
        }

        GoogleIdToken.Payload payload = idToken.getPayload();
        User user = userRepository.findByEmail(payload.getEmail())
                .orElseGet(() -> createUser(payload));

        Session session = createSession(user);
        userRepository.save(user);
        return Optional.of(session.getToken());
    }

    private Session createSession(User user) {
        Session session = new Session(UUID.randomUUID().toString(), user);
        user.getSessions().add(session);
        return session;
    }

    private User createUser(GoogleIdToken.Payload payload) {
        return User.builder()
                .email(payload.getEmail())
                .roles(List.of(Role.USER))
                .idProvider("GOOGLE")   // for now - hardcoded
                .externalId(payload.getSubject())
                .name(String.valueOf(payload.get("name")))
                .familyName(String.valueOf(payload.get("family_name")))
                .givenName(String.valueOf(payload.get("given_name")))
                .pictureUrl(String.valueOf(payload.get("picture")))
                .sessions(new ArrayList<>())
                .build();
    }
}
