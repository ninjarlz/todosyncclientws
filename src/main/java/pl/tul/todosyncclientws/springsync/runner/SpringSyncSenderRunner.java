package pl.tul.todosyncclientws.springsync.runner;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.stereotype.Component;
import org.springframework.sync.Patch;
import org.springframework.sync.ReplaceOperation;
import org.springframework.sync.TestOperation;
import org.springframework.sync.diffsync.exception.PersistenceCallbackNotFoundException;
import org.springframework.sync.diffsync.service.DiffSyncService;
import org.springframework.sync.diffsync.shadowstore.ShadowStore;
import org.springframework.sync.exception.PatchException;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import pl.tul.todosyncclientws.callback.TodoPersistenceCallback;
import pl.tul.todosyncclientws.gateway.TodoGateway;
import pl.tul.todosyncclientws.springsync.stomp.PatchStompSessionHandler;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@Profile("springSyncSender")
@Log4j2
public class SpringSyncSenderRunner extends SpringSyncRunner {

    private static final ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(8);
    private static long ADD_DELAY = 3L;

    @Autowired
    public SpringSyncSenderRunner(TodoPersistenceCallback todoPersistenceCallback, DiffSyncService diffSyncService, ShadowStore webSocketShadowStore, TodoGateway todoGateway, PatchStompSessionHandler sessionHandler, WebSocketStompClient webSocketStompClient) {
        super(todoPersistenceCallback, diffSyncService, webSocketShadowStore, todoGateway, sessionHandler, webSocketStompClient);
    }

    @Override
    public void run(String... args) throws PersistenceCallbackNotFoundException, PatchException, InterruptedException, ExecutionException {
        fetchInitialData();
        changeRecordStatusAsync();
    }

    private void changeRecordStatusAsync() {
        EXECUTOR.schedule(this::changeRecordStatus, ADD_DELAY, TimeUnit.SECONDS);
    }

    @SneakyThrows
    private void changeRecordStatus() {
        StompSession session = webSocketStompClient.connect("ws://" + serverIP + ":8080/sync/websocket", sessionHandler).get();
        TestOperation testOperation = new TestOperation("/complete", false);
        ReplaceOperation replaceOperation = new ReplaceOperation("/complete", true);
        sessionHandler.sendPatch(session, "/app/" + RESOURCE_PATH + "/2", new Patch(List.of(testOperation, replaceOperation)));
    }
}
