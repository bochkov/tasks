package sb.tasks.web.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public final class JsonAnswer {

    public static final JsonAnswer OK = new JsonAnswer(Boolean.TRUE);
    public static final JsonAnswer FAIL = new JsonAnswer(Boolean.FALSE);

    private final Boolean success;

}
