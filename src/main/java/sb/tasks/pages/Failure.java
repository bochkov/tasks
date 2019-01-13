package sb.tasks.pages;

import lombok.Data;

@Data
public final class Failure {

    private final boolean success;

    public Failure() {
        this.success = false;
    }
}
