package sb.tasks.notif.telegram.answers;

import com.mongodb.client.MongoDatabase;

public final class AnsNormArgs implements Answer {

    private final Answer origin;

    public AnsNormArgs(Answer origin) {
        this.origin = origin;
    }

    @Override
    public void handle(String chatId, String[] args) {
        String url = args[0];
        String[] pass = new String[args.length];
        System.arraycopy(args, 1, pass, 1, args.length - 1);
        if (url.matches("^https?://www.lostfilm.tv/.*"))
            pass[0] = url.replaceFirst("https?://www.lostfilm.tv", "https://lostfilm.tv");
        else
            pass[0] = url;
        this.origin.handle(chatId, pass);
    }

    @Override
    public MongoDatabase db() {
        return this.origin.db();
    }

    @Override
    public String token() {
        return this.origin.token();
    }
}
