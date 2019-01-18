package sb.tasks.pages;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public final class Ids {

    private final List<String> id;

    public Ids(@JsonProperty("id") List<String> id) {
        this.id = id;
    }

    public List<String> getAll() {
        return id;
    }
}
