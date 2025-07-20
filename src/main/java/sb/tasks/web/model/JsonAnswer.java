package sb.tasks.web.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public final class JsonAnswer {

    private final Boolean success;
    private final String msg;

    public static JsonAnswer ok() {
        return new JsonAnswer(Boolean.TRUE, null);
    }

    public static JsonAnswer fail(String cause) {
        return new JsonAnswer(Boolean.FALSE, cause);
    }

}
