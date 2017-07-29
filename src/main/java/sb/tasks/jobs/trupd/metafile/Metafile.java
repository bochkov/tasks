package sb.tasks.jobs.trupd.metafile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Date;

public final class Metafile extends Bencode implements Mt {

    private final byte[] body;

    public Metafile(byte[] body) throws IOException {
        super(new ByteArrayInputStream(body));
        this.body = body;
    }

    @Override
    public String name() {
        return new String(
                ((ByteBuffer) info().get(key("name"))).array());
    }

    @Override
    public Date creationDate() {
        return new Date((Long) rootElement().get(key("creation date")) * 1000);
    }

    @Override
    public byte[] body() {
        return body;
    }
}
