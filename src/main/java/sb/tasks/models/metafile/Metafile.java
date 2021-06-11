package sb.tasks.models.metafile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.SortedMap;

public final class Metafile extends Bencode implements Mt {

    private final byte[] body;

    public Metafile(byte[] body) throws IOException {
        super(new ByteArrayInputStream(body));
        this.body = body;
    }

    @Override
    public String name() {
        var map = (SortedMap<?, ?>) rootElement.get(key("info"));
        var bb = (ByteBuffer) map.get(key("name"));
        return new String(bb.array());
    }

    @Override
    public Date creationDate() {
        long dt = (Long) rootElement.get(key("creation date")) * 1000;
        return new Date(dt);
    }

    @Override
    public byte[] body() {
        return body;
    }
}
