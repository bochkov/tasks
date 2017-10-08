package sb.tasks.telegram.pojos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public final class Update {

    @JsonProperty("update_id")
    private Integer id;
    private Message message;

}
