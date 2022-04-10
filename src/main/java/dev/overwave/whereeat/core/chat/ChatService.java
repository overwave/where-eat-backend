package dev.overwave.whereeat.core.chat;

import it.tdlight.client.APIToken;
import it.tdlight.client.AuthenticationData;
import it.tdlight.client.GenericUpdateHandler;
import it.tdlight.client.Result;
import it.tdlight.client.SimpleTelegramClient;
import it.tdlight.client.TDLibSettings;
import it.tdlight.common.Init;
import it.tdlight.jni.TdApi;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

import static it.tdlight.jni.TdApi.AuthorizationState;
import static it.tdlight.jni.TdApi.AuthorizationStateReady;
import static it.tdlight.jni.TdApi.Update;
import static it.tdlight.jni.TdApi.UpdateAuthorizationState;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {
    private final BuildProperties buildProperties;

    private final CountDownLatch authorizationLock = new CountDownLatch(1);

    private SimpleTelegramClient client;

    @Value("${whereeat.login.phone}")
    private long loginPhone;

    @Value("${whereeat.login.api-id}")
    private int apiId;

    @Value("${whereeat.login.api-hash}")
    private String apiHash;

    @Value("${whereeat.path.database}")
    private String databasePath;

    @Value("${whereeat.path.downloads}")
    private String downloadsPath;

    @PostConstruct
    private void initClient() {
        client = startClient();
    }

    @SneakyThrows
    private SimpleTelegramClient startClient() {
        Init.start();
        APIToken apiToken = new APIToken(apiId, apiHash);
        TDLibSettings settings = TDLibSettings.create(apiToken);

        settings.setApplicationVersion(buildProperties.getVersion());
        settings.setDatabaseDirectoryPath(Paths.get(databasePath));
        settings.setDownloadedFilesDirectoryPath(Paths.get(downloadsPath));

        SimpleTelegramClient client = new SimpleTelegramClient(settings);

        AuthenticationData authenticationData = new PhoneAuthentication(loginPhone);

        client.addUpdateHandler(UpdateAuthorizationState.class, this::authorizationHandler);

        client.start(authenticationData);
        authorizationLock.await();
        return client;
    }

    @SneakyThrows
    private void authorizationHandler(UpdateAuthorizationState update) {
        AuthorizationState state = update.authorizationState;

        if (state instanceof AuthorizationStateReady) {
            log.info("Authorization success");
            authorizationLock.countDown();
        }
    }

    public <T extends TdApi.Object> T sendSynchronously(TdApi.Function<T> function) {
        return sendAsynchronously(function).join();
    }

    public <T extends TdApi.Object> CompletableFuture<T> sendAsynchronously(TdApi.Function<T> function) {
        CompletableFuture<Result<T>> future = new CompletableFuture<>();
        client.send(function, future::complete);
        return future.thenApply(Result::get);
    }

    public <T extends Update> void addUpdateHandler(Class<T> updateType, GenericUpdateHandler<T> handler) {
        client.addUpdateHandler(updateType, handler);
    }

    @PreDestroy
    private void releaseClient() throws InterruptedException {
        client.closeAndWait();
    }
}
