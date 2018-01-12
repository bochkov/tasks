package sb.tasks.telegram.pojos;

import lombok.Data;

@Data
public final class MessageEntity {
    private String type;
    private Integer offset;
    private Integer length;
    private String url;
    private User user;
}
