package sb.tasks.model;

import sb.tasks.util.Bencode;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

public final class Metafile {

    private final byte[] body;
    private final Map<String, Object> parsed = new HashMap<>();

    public Metafile(byte[] body) {
        this.body = body;
        try {
            Map<?, ?> map = (Map<?, ?>) new Bencode().parse(new ByteArrayInputStream(body));
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (entry.getKey() instanceof String key) {
                    parsed.put(key, entry.getValue());
                }
            }
        } catch (IOException ex) {
            //
        }
    }

    public String name() {
        Map<?, ?> map = (Map<?, ?>) parsed.get("info");
        return (String) map.get("name");
    }

    public LocalDateTime creationDate() {
        long dt = (Long) parsed.get("creation date");
        return LocalDateTime.ofInstant(
                Instant.ofEpochSecond(dt),
                ZoneId.systemDefault()
        );
    }

    public byte[] body() {
        return body;
    }
}
