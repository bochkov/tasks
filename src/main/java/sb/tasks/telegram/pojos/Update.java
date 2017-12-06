package sb.tasks.telegram.pojos;

import com.fasterxml.jackson.annotation.JsonProperty;

public final class Update {

    @JsonProperty("update_id")
    private Integer id;
    private Message message;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }
}
