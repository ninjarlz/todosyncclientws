package pl.tul.todosyncclientws.springsync.runner;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.sync.AddOperation;
import org.springframework.sync.Patch;
import org.springframework.sync.PatchOperation;
import org.springframework.sync.diffsync.exception.PersistenceCallbackNotFoundException;
import org.springframework.sync.diffsync.service.DiffSyncService;
import org.springframework.sync.diffsync.shadowstore.ShadowStore;
import org.springframework.sync.exception.PatchException;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import pl.tul.todosyncclientws.callback.TodoPersistenceCallback;
import pl.tul.todosyncclientws.data.Todo;
import pl.tul.todosyncclientws.gateway.TodoGateway;
import pl.tul.todosyncclientws.springsync.stomp.PatchStompSessionHandler;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.IntStream;

@RequiredArgsConstructor
public abstract class SpringSyncRunner implements CommandLineRunner {

    protected static final String COLLECTION_PATH = "todos";
    @Value("${spring-sync.server-ip}")
    protected String serverIP;

    protected final TodoPersistenceCallback todoPersistenceCallback;
    protected final DiffSyncService diffSyncService;
    protected final ShadowStore webSocketShadowStore;
    protected final TodoGateway todoGateway;
    protected final PatchStompSessionHandler sessionHandler;
    protected final WebSocketStompClient webSocketStompClient;

    protected void fetchInitialData() throws PersistenceCallbackNotFoundException, PatchException, ExecutionException, InterruptedException {
        List<Todo> todoList = todoGateway.getAll();
        List<PatchOperation> addOperations = IntStream.range(0, todoList.size())
                .mapToObj(index -> new AddOperation("/" + index, todoList.get(index)))
                .map(addOperation -> (PatchOperation) addOperation)
                .toList();
        diffSyncService.patch(webSocketShadowStore, COLLECTION_PATH, new Patch(addOperations));
        assert todoPersistenceCallback.count() == 3;
    }
}
