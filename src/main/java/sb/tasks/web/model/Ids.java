package sb.tasks.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public final class Ids {

    @JsonProperty("id")
    private List<String> ids;


}
