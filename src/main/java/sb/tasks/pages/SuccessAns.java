package sb.tasks.pages;

import lombok.Data;

@Data
public final class SuccessAns implements HttpAnswer {

    private final boolean success;

    public SuccessAns() {
        this.success = true;
    }
}
