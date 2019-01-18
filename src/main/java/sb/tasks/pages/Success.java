package sb.tasks.pages;

import lombok.Data;

@Data
public final class Success implements HttpAnswer {

    private final boolean success;

    public Success() {
        this.success = true;
    }
}
