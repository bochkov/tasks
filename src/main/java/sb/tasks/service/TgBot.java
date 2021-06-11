package sb.tasks.service;

import kong.unirest.Unirest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import resnyx.methods.message.SendMessage;

@Slf4j
@RequiredArgsConstructor
public final class TgBot {

    private final String token;

    public void send(Long chatId, String text) {
        var msg = new SendMessage(token, chatId, text);
        msg.setParseMode("html");
        msg.setDisablePreview(true);
        Unirest.post("https://resnyx.sergeybochkov.com/tg")
                .header("Content-Type", "application/json")
                .body(msg)
                .asEmpty();
    }
}
