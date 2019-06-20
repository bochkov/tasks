package sb.tasks.system.net;

import com.jcabi.http.Request;
import com.jcabi.http.Response;
import com.jcabi.http.Wire;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

public final class GzipWire implements Wire {

    private final Wire origin;

    public GzipWire(Wire origin) {
        this.origin = origin;
    }

    @Override
    public Response send(Request req, String home, String method, Collection<Map.Entry<String, String>> headers,
                         InputStream content, int connect, int read) throws IOException {
        return new GzipResponse(
                this.origin.send(req, home, method, headers, content, connect, read)
        );
    }
}
