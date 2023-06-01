package pl.tul.todosyncclientws.firebase.runner;

import com.google.cloud.firestore.Firestore;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import pl.tul.todosyncclientws.callback.TodoPersistenceCallback;
import pl.tul.todosyncclientws.data.Todo;
import pl.tul.todosyncclientws.firebase.gateway.FirebaseTodoGateway;

@Component
@Log4j2
@Profile("firebaseSender")
public class FirebaseSenderRunner extends FirebaseRunner {

    @Autowired
    public FirebaseSenderRunner(Firestore firestore, FirebaseTodoGateway firebaseTodoGateway, TodoPersistenceCallback todoPersistenceCallback) {
        super(firestore, firebaseTodoGateway, todoPersistenceCallback);
    }

    @Override
    public void run(String... args) throws Exception {
        fetchInitialData();
        Todo todo = Todo.builder()
                .id(4L)
                .description("D")
                .complete(false)
                .build();
        log.error("SENT TIME: {}", System.currentTimeMillis());
        todo = firestore.collection(COLLECTION_PATH)
                .add(todo)
                .get()
                .get()
                .get()
                .toObject(Todo.class);
        todoPersistenceCallback.persistChange(todo);
        log.info(todoPersistenceCallback.findAll());
    }
}
