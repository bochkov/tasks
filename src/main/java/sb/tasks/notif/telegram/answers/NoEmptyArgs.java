package sb.tasks.notif.telegram.answers;

import com.mongodb.client.MongoDatabase;
import sb.tasks.notif.telegram.BotAnswer;

public final class NoEmptyArgs implements Answer {

    private final Answer origin;
    private final String msg;

    public NoEmptyArgs(String msg, Answer origin) {
        this.origin = origin;
        this.msg = msg;
    }

    @Override
    public void handle(String chatId, String[] args) {
        if (args.length == 0)
            new BotAnswer(token()).send(chatId, msg);
        else
            this.origin.handle(chatId, args);
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
