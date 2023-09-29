package pl.tul.todosyncclientws.firebase.runner;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import pl.tul.todosyncclientws.callback.TodoPersistenceCallback;
import pl.tul.todosyncclientws.data.Todo;
import pl.tul.todosyncclientws.firebase.gateway.FirebaseTodoGateway;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Component
@Log4j2
@Profile("firebaseSender")
public class FirebaseSenderRunner extends FirebaseRunner {

    private static final String SENDING_MSG = "Sending message to: {}, time: {}";
    private static final String SENT_MSG = "Sent message to: {}, time: {}";
    private static final Executor EXECUTOR = Executors.newCachedThreadPool();

    @Autowired
    public FirebaseSenderRunner(Firestore firestore, FirebaseTodoGateway firebaseTodoGateway, TodoPersistenceCallback todoPersistenceCallback) {
        super(firestore, firebaseTodoGateway, todoPersistenceCallback);
    }

    @Override
    public void run(String... args) throws Exception {
        fetchInitialData();
        Thread.sleep(3000);
        Todo todo = Todo.builder()
                .id(4L)
                .description("D")
                .complete(false)
                .build();
        log.error(SENDING_MSG, COLLECTION_PATH, System.currentTimeMillis());
        ApiFuture<DocumentSnapshot> apiFuture = firestore.collection(COLLECTION_PATH)
                .add(todo)
                .get()
                .get();
        ApiFutures.addCallback(apiFuture, new UpdateSentCallback(), EXECUTOR);
        todo =  apiFuture
                .get()
                .toObject(Todo.class);
        todoPersistenceCallback.persistChange(todo);
        log.info(todoPersistenceCallback.findAll());
    }

    private static class UpdateSentCallback implements ApiFutureCallback<DocumentSnapshot> {
        @Override
        public void onFailure(Throwable throwable) {
            log.error(throwable);
            throw new RuntimeException(throwable);
        }

        @Override
        public void onSuccess(DocumentSnapshot documentSnapshot) {
            log.error(SENT_MSG, COLLECTION_PATH, System.currentTimeMillis());
        }
    }
}
