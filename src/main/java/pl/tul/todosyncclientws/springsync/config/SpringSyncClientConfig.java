package pl.tul.todosyncclientws.springsync.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.sync.diffsync.Equivalency;
import org.springframework.sync.diffsync.IdPropertyEquivalency;
import org.springframework.sync.diffsync.PersistenceCallbackRegistry;
import org.springframework.sync.diffsync.service.DiffSyncService;
import org.springframework.sync.diffsync.service.impl.DiffSyncServiceImpl;
import org.springframework.sync.diffsync.shadowstore.MapBasedShadowStore;
import org.springframework.sync.diffsync.shadowstore.ShadowStore;
import org.springframework.sync.diffsync.web.JsonPatchWebSocketMessageConverter;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import pl.tul.todosyncclientws.callback.TodoPersistenceCallback;
import pl.tul.todosyncclientws.springsync.stomp.PatchStompSessionHandler;

@Configuration
@Profile({"springSyncSender", "springSyncReceiver"})
public class SpringSyncClientConfig {
    @Value("${spring-sync.server-url}")
    private String serverUrl;
    @Bean
    public ShadowStore webSocketShadowStore() {
        return new MapBasedShadowStore("server");
    }

    @Bean
    public PersistenceCallbackRegistry persistenceCallbackRegistry(TodoPersistenceCallback todoPersistenceCallback) {
        PersistenceCallbackRegistry registry = new PersistenceCallbackRegistry();
        registry.addPersistenceCallback(todoPersistenceCallback);
        return registry;
    }

    @Bean
    public Equivalency equivalency() {
        return new IdPropertyEquivalency();
    }

    @Bean
    public WebClient webClient() {
        return WebClient.create(serverUrl);
    }

    @Bean
    public WebSocketStompClient webSocketStompClient() {
        WebSocketClient client = new StandardWebSocketClient();
        WebSocketStompClient stompClient = new WebSocketStompClient(client);
        stompClient.setMessageConverter(new JsonPatchWebSocketMessageConverter());
        return stompClient;
    }

    @Bean
    public DiffSyncService diffSyncService(PersistenceCallbackRegistry persistenceCallbackRegistry, Equivalency equivalency) {
        return new DiffSyncServiceImpl(persistenceCallbackRegistry, equivalency);
    }

    @Bean
    public PatchStompSessionHandler sessionHandler(DiffSyncService diffSyncService, TodoPersistenceCallback todoPersistenceCallback, ShadowStore webSocketShadowStore) {
        return new PatchStompSessionHandler(diffSyncService, todoPersistenceCallback, webSocketShadowStore);
    }
}
