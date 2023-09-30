package pl.tul.todosyncclientws.firebase.runner;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import pl.tul.todosyncclientws.callback.TodoPersistenceCallback;
import pl.tul.todosyncclientws.data.Todo;
import pl.tul.todosyncclientws.firebase.gateway.FirebaseTodoGateway;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@Log4j2
@Profile("firebaseSender")
public class FirebaseSenderRunner extends FirebaseRunner {

    private static final String SENDING_MSG = "Sending message to: {}, time: {}";
    private static final ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(8);
    private static long ADD_DELAY = 3L;

    @Autowired
    public FirebaseSenderRunner(Firestore firestore, FirebaseTodoGateway firebaseTodoGateway, TodoPersistenceCallback todoPersistenceCallback) {
        super(firestore, firebaseTodoGateway, todoPersistenceCallback);
    }

    @Override
    public void run(String... args) throws Exception {
        fetchInitialData();
        changeRecordStatusAsync();
    }

    private void changeRecordStatusAsync() {
        EXECUTOR.schedule(this::changeRecordStatus, ADD_DELAY, TimeUnit.SECONDS);
    }

    @SneakyThrows
    private void changeRecordStatus() {
        log.error(SENDING_MSG, COLLECTION_PATH, System.currentTimeMillis());
        ApiFuture<Void> apiFuture = firestore.runTransaction(transaction -> {
            DocumentReference documentReference = firestore.collection(COLLECTION_PATH).document("2");
            transaction.update(documentReference, "complete", true);
            return null;
        });
        apiFuture.get();
        Todo todo = firestore.collection(COLLECTION_PATH)
                .document("2")
                .get()
                .get()
                .toObject(Todo.class);
        todoPersistenceCallback.persistChange(todo);
        log.info(todoPersistenceCallback.findAll());
    }
}
