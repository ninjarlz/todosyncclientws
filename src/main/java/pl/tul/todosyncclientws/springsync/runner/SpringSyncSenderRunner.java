package pl.tul.todosyncclientws.springsync.runner;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.stereotype.Component;
import org.springframework.sync.AddOperation;
import org.springframework.sync.Patch;
import org.springframework.sync.diffsync.exception.PersistenceCallbackNotFoundException;
import org.springframework.sync.diffsync.service.DiffSyncService;
import org.springframework.sync.diffsync.shadowstore.ShadowStore;
import org.springframework.sync.exception.PatchException;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import pl.tul.todosyncclientws.callback.TodoPersistenceCallback;
import pl.tul.todosyncclientws.data.Todo;
import pl.tul.todosyncclientws.gateway.TodoGateway;
import pl.tul.todosyncclientws.springsync.stomp.PatchStompSessionHandler;

import java.util.Collections;
import java.util.concurrent.ExecutionException;

@Component
@Profile("springSyncSender")
@Log4j2
public class SpringSyncSenderRunner extends SpringSyncRunner {

    @Autowired
    public SpringSyncSenderRunner(TodoPersistenceCallback todoPersistenceCallback, DiffSyncService diffSyncService, ShadowStore webSocketShadowStore, TodoGateway todoGateway, PatchStompSessionHandler sessionHandler, WebSocketStompClient webSocketStompClient) {
        super(todoPersistenceCallback, diffSyncService, webSocketShadowStore, todoGateway, sessionHandler, webSocketStompClient);
    }

    @Override
    public void run(String... args) throws PersistenceCallbackNotFoundException, PatchException, InterruptedException, ExecutionException {
        fetchInitialData();
        StompSession session = webSocketStompClient.connect("ws://" + serverIP + ":8080/sync/websocket", sessionHandler).get();
        Thread.sleep(3000);
        Todo todoToAdd = Todo.builder().description("D").complete(false).build();
        AddOperation addOperation = new AddOperation("/3", todoToAdd);
        sessionHandler.sendPatch(session, "/app/todos", new Patch(Collections.singletonList(addOperation)));
    }
}
