package sb.tasks.telegram.pojos;

import com.fasterxml.jackson.annotation.JsonProperty;

public final class ChatPhoto {

    @JsonProperty("small_file_id")
    private String smallFileId;

    @JsonProperty("big_file_id")
    private String bigFileId;

    public void setSmallFileId(String smallFileId) {
        this.smallFileId = smallFileId;
    }

    public void setBigFileId(String bigFileId) {
        this.bigFileId = bigFileId;
    }
}
