package sb.tasks.pages;

import lombok.Data;

@Data
public final class FailureAns implements HttpAnswer {

    private final boolean success;

    public FailureAns() {
        this.success = false;
    }
}
