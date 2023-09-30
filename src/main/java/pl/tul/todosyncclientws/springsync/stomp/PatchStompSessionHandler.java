package pl.tul.todosyncclientws.springsync.stomp;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.stereotype.Component;
import org.springframework.sync.Patch;
import org.springframework.sync.diffsync.exception.PersistenceCallbackNotFoundException;
import org.springframework.sync.diffsync.service.DiffSyncService;
import org.springframework.sync.diffsync.shadowstore.ShadowStore;
import org.springframework.sync.exception.PatchException;
import pl.tul.todosyncclientws.callback.TodoPersistenceCallback;
import pl.tul.todosyncclientws.data.Todo;

import java.lang.reflect.Type;
import java.util.List;

@Log4j2
@Component
@RequiredArgsConstructor
@Profile({"springSyncSender", "springSyncReceiver"})
public class PatchStompSessionHandler extends StompSessionHandlerAdapter {

    private static final String RESOURCE_PATH = "todos";
    private static final String RESOURCE_TOPIC_PATH = "/topic/" + RESOURCE_PATH;
    private static final String SESSION_ESTABLISHED_MSG = "New session established: {}";
    private static final String SUBSCRIBED_MSG = "Subscribed to: {}";
    private static final String ERROR_MSG = "WebSocket error occurred: {}";
    private static final String RECEIVED_MSG = "Received message: {}, time: {}";
    private static final String SENDING_MSG = "Sending message to: {}, time: {}";
    private static final String PATCH_APPLIED_MSG = "Patch applied";
    private static final String PATCH_NOT_APPLIED_MSG = "Patch cannot be applied: {}";

    private final DiffSyncService diffSyncService;
    private final TodoPersistenceCallback todoPersistenceCallback;
    private final ShadowStore webSocketShadowStore;

    @Override
    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
        log.info(SESSION_ESTABLISHED_MSG, session.getSessionId());
        session.subscribe(RESOURCE_TOPIC_PATH, this);
        log.info(SUBSCRIBED_MSG, RESOURCE_TOPIC_PATH);
    }

    @Override
    public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
        log.error(ERROR_MSG, exception);
    }

    @Override
    public Type getPayloadType(StompHeaders headers) {
        return Patch.class;
    }

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
        log.info(RECEIVED_MSG, headers, System.currentTimeMillis());
        Patch patch = (Patch) payload;
        applyPatch(patch);
        List<Todo> todoList = todoPersistenceCallback.findAll();
        log.info(todoList);
    }

    public void sendPatch(StompSession session, String destination, Patch patch) {
        log.error(SENDING_MSG, destination, System.currentTimeMillis());
        session.send(destination, patch);
        applyPatch(patch);
        List<Todo> todoList = todoPersistenceCallback.findAll();
        log.info(todoList);
    }

    public void applyPatch(Patch patch) {
        try {
            diffSyncService.patch(webSocketShadowStore, RESOURCE_PATH, patch);
            log.info(PATCH_APPLIED_MSG);
        } catch (PatchException | PersistenceCallbackNotFoundException e) {
            log.error(PATCH_NOT_APPLIED_MSG, e);
            throw new RuntimeException(e);
        }
    }
}
