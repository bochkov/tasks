package sb.tasks.models.http;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public final class Ids {

    private final List<String> id;

    public Ids(@JsonProperty("id") List<String> id) {
        this.id = id;
    }

    public List<String> getAll() {
        return id;
    }
}
