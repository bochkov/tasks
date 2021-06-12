package sb.tasks.service;

import java.io.IOException;

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
        try {
            msg.execute();
        } catch (IOException ex) {
            LOG.warn(ex.getMessage(), ex);
        }
    }
}
