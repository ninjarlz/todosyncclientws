package pl.tul.todosyncclientws.springsync.gateway;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import pl.tul.todosyncclientws.data.Todo;
import pl.tul.todosyncclientws.gateway.TodoGateway;

import java.util.List;

@Component
@Profile({"springSyncSender", "springSyncReceiver"})
@RequiredArgsConstructor
@Log4j2
public class SpringSyncTodoGateway implements TodoGateway {

    private static final ParameterizedTypeReference<List<Todo>> TODO_LIST_TYPE = new ParameterizedTypeReference<>() {};

    private final WebClient webClient;

    @Override
    public List<Todo> getAll() {
        log.info("Fetching data from: {}", "/todos");
        return webClient.get()
                .uri("/todos")
                .retrieve()
                .bodyToMono(TODO_LIST_TYPE)
                .block();
    }
}
