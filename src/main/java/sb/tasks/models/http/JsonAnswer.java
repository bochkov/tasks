package sb.tasks.models.http;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode
@ToString
public final class JsonAnswer {

    public static final JsonAnswer OK = new JsonAnswer(Boolean.TRUE);
    public static final JsonAnswer FAIL = new JsonAnswer(Boolean.FALSE);

    private final Boolean success;

    private JsonAnswer(Boolean success) {
        this.success = success;
    }
}
