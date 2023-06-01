package pl.tul.todosyncclientws.gateway;

import pl.tul.todosyncclientws.data.Todo;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface TodoGateway {
    List<Todo> getAll() throws InterruptedException, ExecutionException;
}
