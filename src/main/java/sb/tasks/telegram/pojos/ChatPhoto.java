package sb.tasks.telegram.pojos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public final class ChatPhoto {
    @JsonProperty("small_file_id")
    private String smallFileId;
    @JsonProperty("big_file_id")
    private String bigFileId;
}
