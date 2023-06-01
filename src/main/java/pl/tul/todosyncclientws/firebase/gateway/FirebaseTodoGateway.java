package pl.tul.todosyncclientws.firebase.gateway;

import com.google.cloud.firestore.Firestore;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import pl.tul.todosyncclientws.data.Todo;
import pl.tul.todosyncclientws.gateway.TodoGateway;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Component
@RequiredArgsConstructor
@Profile({"firebaseSender", "firebaseReceiver"})
public class FirebaseTodoGateway implements TodoGateway {

    protected final Firestore firestore;

    @Override
    public List<Todo> getAll() throws ExecutionException, InterruptedException {
        return firestore.collection("todos")
                .get()
                .get()
                .toObjects(Todo.class);
    }
}
