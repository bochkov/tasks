package sb.tasks.telegram.pojos;

import com.fasterxml.jackson.annotation.JsonProperty;

public final class ChatPhoto {

    @JsonProperty("small_file_id")
    private String smallFileId;

    @JsonProperty("big_file_id")
    private String bigFileId;

    public String getSmallFileId() {
        return smallFileId;
    }

    public void setSmallFileId(String smallFileId) {
        this.smallFileId = smallFileId;
    }

    public String getBigFileId() {
        return bigFileId;
    }

    public void setBigFileId(String bigFileId) {
        this.bigFileId = bigFileId;
    }
}
