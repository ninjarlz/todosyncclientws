package pl.tul.todosyncclientws.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Todo implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    private String description;

    private boolean complete;
}
