package pl.tul.todosyncclientws.callback;

import org.apache.commons.beanutils.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.sync.diffsync.PersistenceCallback;
import org.springframework.sync.diffsync.exception.ResourceNotFoundException;
import pl.tul.todosyncclientws.data.Todo;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class TodoPersistenceCallback implements PersistenceCallback<Todo> {

    private final List<Todo> data;

    public TodoPersistenceCallback() {
        this.data = new ArrayList<>();
    }

    public int count() {
        return data.size();
    }

    @Override
    public List<Todo> findAll() {
        return List.copyOf(data);
    }

    @Override
    public Todo findOne(String id) throws ResourceNotFoundException {
        Long longId = Long.parseLong(id);
        return data.stream()
                .filter(todo -> longId.equals(todo.getId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(id));
    }

    @Override
    public void persistChange(Todo t) {
        data.stream()
                .filter(todo -> Objects.equals(t.getId(),todo.getId()))
                .findFirst()
                .ifPresentOrElse(todo -> copyProperties(todo, t), () -> data.add(t));
    }

    @Override
    public void persistChanges(List<Todo> itemsToSave, List<Todo> itemsToDelete) {
        itemsToSave.forEach(this::persistChange);
        itemsToDelete.forEach(data::remove);
    }

    @Override
    public Class<Todo> getEntityType() {
        return Todo.class;
    }

    private void copyProperties(Todo target, Todo source) {
        try {
            BeanUtils.copyProperties(target, source);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

}
