package pl.tul.todosyncclientws.firebase.runner;

import com.google.cloud.firestore.Firestore;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import pl.tul.todosyncclientws.callback.TodoPersistenceCallback;
import pl.tul.todosyncclientws.data.Todo;
import pl.tul.todosyncclientws.firebase.gateway.FirebaseTodoGateway;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;


@RequiredArgsConstructor
public abstract class FirebaseRunner implements CommandLineRunner {

    protected static final String COLLECTION_PATH = "todos";

    protected final Firestore firestore;
    protected final FirebaseTodoGateway firebaseTodoGateway;
    protected final TodoPersistenceCallback todoPersistenceCallback;

    protected void fetchInitialData() throws ExecutionException, InterruptedException {
        List<Todo> todoList = firebaseTodoGateway.getAll();
        todoPersistenceCallback.persistChanges(todoList, Collections.emptyList());
        assert todoPersistenceCallback.count() == 3;
    }
}
