package sb.tasks.telegram.pojos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public final class Message {

    @JsonProperty("message_id")
    private Integer id;
    private User from;
    private Date date;
    private Chat chat;
    private String text;
    private List<MessageEntity> entities;
}
