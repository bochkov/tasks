package sb.tasks.model;

import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import sb.tasks.jobs.Trupd;

import java.time.LocalDateTime;

@Slf4j
@Data
@ToString(exclude = "registered")
@Document(collection = Task.COLLECTION)
public final class Task {

    public static final String COLLECTION = "tasks";

    @Id
    private String id;

    @Field
    private Vars vars;

    @Field
    private String job;

    @Field
    private Params params;

    @Field(name = "schedule")
    private String[] schedules;

    @Data
    public static final class Vars {

        @Field
        private LocalDateTime checked;
        @Field
        private LocalDateTime created;
        @Field(name = "download_date")
        private LocalDateTime downloadDate;
        @Field(name = "download_url")
        private String downloadUrl;
        @Field
        private String name;

    }

    @Data
    public static final class Params {

        @Field(name = "download_dir")
        private String downloadDir;
        @Field(name = "mail_to")
        private String[] mailTo;
        @Field
        private String subject;
        @Field
        private String telegram;
        @Field(name = "admin_telegram")
        private String adminTelegram;
        @Field
        private String text;
        @Field
        private String url;

    }

    @Transient
    private boolean registered;

    public static Task defaultTask(String url, String dir, Long chatId) {
        Task task = new Task();
        task.job = Trupd.class.getCanonicalName();
        task.params = new Params();
        task.params.url = url;
        task.params.downloadDir = dir;
        task.params.telegram = String.valueOf(chatId);
        task.vars = new Vars();
        task.vars.name = "NOT EVALUATED";
        task.schedules = new String[]{"0 0 * * * ?"};
        LOG.info("Added task {}, {{}, dir={}, telegram={}", task.job, url, dir, chatId);
        return task;
    }

}
