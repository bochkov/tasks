package sb.tasks.telegram.pojos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public final class Chat {

    private String id;
    private String type;
    private String title;
    private String username;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    @JsonProperty("all_members_are_administrators")
    private Boolean allAdmin;

    private ChatPhoto photo;
    private String description;

    @JsonProperty("invite_link")
    private String inviteLink;
}
