package pl.tul.todosyncclientws.firebase.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.io.IOException;
import java.io.InputStream;

@Configuration
@Profile({"firebaseSender", "firebaseReceiver"})
public class FirebaseClientConfig {

    private static final String SERVICE_ACCOUNT_FILE = "/todosync-87088-c5a0bf0ef547.json";

    @Bean
    public Firestore firestore() throws IOException {
        InputStream serviceAccount = FirebaseClientConfig.class.getResourceAsStream(SERVICE_ACCOUNT_FILE);
        GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);
        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(credentials)
                .build();
        FirebaseApp.initializeApp(options);
        return FirestoreClient.getFirestore();
    }
}
