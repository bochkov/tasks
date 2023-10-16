package sb.tasks.service.tgbot;

import java.util.Map;

import kong.unirest.core.ContentType;
import kong.unirest.core.HttpRequestWithBody;
import kong.unirest.core.Unirest;
import kong.unirest.core.UnirestInstance;
import kong.unirest.jackson.JacksonObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import resnyx.TgMethod;
import resnyx.common.ParseMode;
import resnyx.messenger.general.SendMessage;
import resnyx.util.TgObjectMapperConfig;

@Slf4j
@RequiredArgsConstructor
public final class TgBot {

    private final String token;

    private void send(TgMethod method) {
        JacksonObjectMapper om = new JacksonObjectMapper(new TgObjectMapperConfig());
        UnirestInstance unirest = Unirest.spawnInstance();
        unirest.config().setObjectMapper(om);

        LOG.info("method = '{}', req = {}", method.methodName(), om.writeValue(method));
        HttpRequestWithBody req = unirest.post(String.format("https://api.telegram.org/bot%s/%s", token, method.methodName()));
        if (method.hasInputFile()) {
            Map<String, Object> fields = method.toValues(om.getJacksonMapper());
            req.contentType(ContentType.MULTIPART_FORM_DATA.getMimeType())
                    .accept(ContentType.APPLICATION_JSON.getMimeType())
                    .fields(fields)
                    .asString()
                    .getBody();
        } else {
            req.contentType(ContentType.APPLICATION_JSON.getMimeType())
                    .body(method)
                    .asString()
                    .getBody();
        }
    }

    public void send(Long chatId, String text) {
        SendMessage msg = new SendMessage(String.valueOf(chatId), text);
        msg.setParseMode(ParseMode.HTML);
        msg.setDisableWebPagePreview(true);
        try {
            send(msg);
        } catch (Exception ex) {
            LOG.warn(ex.getMessage(), ex);
        }
    }

}
