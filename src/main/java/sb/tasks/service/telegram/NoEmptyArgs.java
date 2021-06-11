package sb.tasks.service.telegram;

import com.mongodb.client.MongoDatabase;
import lombok.RequiredArgsConstructor;
import sb.tasks.service.TgBot;

@RequiredArgsConstructor
public final class NoEmptyArgs implements Answer {

    private final String msg;
    private final Answer origin;

    @Override
    public void handle(Long chatId, String[] args) {
        if (args.length == 0)
            tgBot().send(chatId, msg);
        else
            this.origin.handle(chatId, args);
    }

    @Override
    public MongoDatabase db() {
        return this.origin.db();
    }

    @Override
    public TgBot tgBot() {
        return origin.tgBot();
    }
}
