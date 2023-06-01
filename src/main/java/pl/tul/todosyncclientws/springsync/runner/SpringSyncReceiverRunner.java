package pl.tul.todosyncclientws.springsync.runner;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.sync.diffsync.exception.PersistenceCallbackNotFoundException;
import org.springframework.sync.diffsync.service.DiffSyncService;
import org.springframework.sync.diffsync.shadowstore.ShadowStore;
import org.springframework.sync.exception.PatchException;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import pl.tul.todosyncclientws.callback.TodoPersistenceCallback;
import pl.tul.todosyncclientws.gateway.TodoGateway;
import pl.tul.todosyncclientws.springsync.stomp.PatchStompSessionHandler;

import java.util.concurrent.ExecutionException;

@Component
@Profile("springSyncReceiver")
@Log4j2
public class SpringSyncReceiverRunner extends SpringSyncRunner {

    @Autowired
    public SpringSyncReceiverRunner(TodoPersistenceCallback todoPersistenceCallback, DiffSyncService diffSyncService, ShadowStore webSocketShadowStore, TodoGateway todoGateway, PatchStompSessionHandler sessionHandler, WebSocketStompClient webSocketStompClient) {
        super(todoPersistenceCallback, diffSyncService, webSocketShadowStore, todoGateway, sessionHandler, webSocketStompClient);
    }

    @Override
    public void run(String... args) throws PersistenceCallbackNotFoundException, PatchException, ExecutionException, InterruptedException {
        fetchInitialData();
        webSocketStompClient.connect("ws://" + serverIP + ":8080/sync/websocket", sessionHandler).get();
    }
}
