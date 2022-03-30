package dev.overwave.whereeat.feed;

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CountDownLatch;

import static it.tdlight.jni.TdApi.*;

@Service
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class FeedService {
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

    @Value("${whereeat.client.database-key}")
    private String databaseEncryptionKey;


    public void getHistory() {
    }

    @PostConstruct
    private void initClient() {
        client = startClient();
//        setTdlibParameters();
//        checkDatabaseEncryption();

    }

    @SneakyThrows
    private SimpleTelegramClient startClient() {
        Init.start();
        APIToken apiToken = new APIToken(apiId, apiHash);
        TDLibSettings settings = TDLibSettings.create(apiToken);

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
        /*if (state instanceof AuthorizationStateWaitTdlibParameters) {
            setTdlibParameters();
        } else if (state instanceof AuthorizationStateWaitCode) {
            System.out.println("enter code");
            String code = new Scanner(System.in).nextLine();
            sendSynchronously(new CheckAuthenticationCode(code));
        } else if (state instanceof AuthorizationStateWaitEncryptionKey) {
            checkDatabaseEncryption();
        } else */
        if (state instanceof AuthorizationStateReady) {
            System.out.println("auth success");
            authorizationLock.countDown();
        } else {
//            System.out.println(state);
        }
    }

    private void setTdlibParameters() {
//        sendSynchronously(new SetTdlibParameters(new TdlibParameters(
//                false,
//                databasePath,
//                downloadsPath,
//                true,
//                true,
//                true,
//                false,
//                apiId,
//                apiHash,
//                "en-US",
//                "where-eat-backend",
//                null,
//                "1.0.0-SNAPSHOT",
//                true,
//                true
//        )));
//        System.out.println("sent");
    }

    private void checkDatabaseEncryption() {
        byte[] encryptionKey = databaseEncryptionKey.getBytes(StandardCharsets.UTF_8);
        try {
            Ok ok = sendSynchronously(new CheckDatabaseEncryptionKey(encryptionKey));
            System.out.println(ok);
        } catch (CompletionException e) {
            sendSynchronously(new SetDatabaseEncryptionKey(encryptionKey));
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
