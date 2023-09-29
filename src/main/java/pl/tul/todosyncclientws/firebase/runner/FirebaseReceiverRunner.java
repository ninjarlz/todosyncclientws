package pl.tul.todosyncclientws.firebase.runner;

import com.google.cloud.firestore.DocumentChange;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreException;
import com.google.cloud.firestore.QuerySnapshot;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import pl.tul.todosyncclientws.callback.TodoPersistenceCallback;
import pl.tul.todosyncclientws.data.Todo;
import pl.tul.todosyncclientws.firebase.gateway.FirebaseTodoGateway;

import java.util.Collections;
import java.util.List;

@Component
@Log4j2
@Profile("firebaseReceiver")
public class FirebaseReceiverRunner extends FirebaseRunner {

    private static final String RECEIVED_MSG = "Received message: {}, time: {}";

    @Autowired
    public FirebaseReceiverRunner(Firestore firestore, FirebaseTodoGateway firebaseTodoGateway, TodoPersistenceCallback todoPersistenceCallback) {
        super(firestore, firebaseTodoGateway, todoPersistenceCallback);
    }

    @Override
    public void run(String... args) {
        firestore.collection(COLLECTION_PATH)
                .addSnapshotListener(this::buildEventListener);
    }

    private void buildEventListener(QuerySnapshot queryDocumentSnapshots, FirestoreException e) {
        log.info(RECEIVED_MSG, queryDocumentSnapshots, System.currentTimeMillis());
        List<Todo> todoList = queryDocumentSnapshots.getDocumentChanges().stream()
                .filter(documentChange -> documentChange.getType().equals(DocumentChange.Type.ADDED))
                .map(documentChange -> documentChange.getDocument().toObject(Todo.class))
                .toList();
        todoPersistenceCallback.persistChanges(todoList, Collections.emptyList());
        log.info(todoPersistenceCallback.findAll());
    }
}
