package sb.tasks.pages;

import lombok.Data;

@Data
public final class Failure implements HttpAnswer {

    private final boolean success;

    public Failure() {
        this.success = false;
    }
}
