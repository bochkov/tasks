package sb.tasks.model;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.SortedMap;

import sb.tasks.util.Bencode;

public final class Metafile extends Bencode {

    private final byte[] body;

    public Metafile(byte[] body) throws IOException {
        super(new ByteArrayInputStream(body));
        this.body = body;
    }

    public String name() {
        var map = (SortedMap<?, ?>) rootElement.get(key("info"));
        var bb = (ByteBuffer) map.get(key("name"));
        return new String(bb.array());
    }

    public LocalDateTime creationDate() {
        long dt = (Long) rootElement.get(key("creation date"));
        return LocalDateTime.ofInstant(
                Instant.ofEpochSecond(dt),
                ZoneId.systemDefault()
        );
    }

    public byte[] body() {
        return body;
    }
}
