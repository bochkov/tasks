package sb.tasks.jobs.trupd;

import com.jcabi.http.Request;
import com.jcabi.http.Response;
import com.jcabi.http.Wire;
import com.jcabi.log.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public final class GzipWire implements Wire {

    private final Wire origin;

    public GzipWire(Wire origin) {
        this.origin = origin;
    }

    @Override
    public Response send(Request req, String home, String method, Collection<Map.Entry<String, String>> headers, InputStream content, int connect, int read) throws IOException {
        return new GzipResponse(
                this.origin.send(req, home, method, headers, content, connect, read)
        );
    }

    public final class GzipResponse implements Response {

        private final Response origin;

        public GzipResponse(Response origin) {
            this.origin = origin;
        }

        @Override
        public Request back() {
            return this.origin.back();
        }

        @Override
        public int status() {
            return this.origin.status();
        }

        @Override
        public String reason() {
            return this.origin.reason();
        }

        @Override
        public Map<String, List<String>> headers() {
            return this.origin.headers();
        }

        @Override
        public String body() {
            try (GZIPInputStream zin = new GZIPInputStream(new ByteArrayInputStream(binary()))) {
                return new String(zin.readAllBytes());
            } catch (IOException ex) {
                Logger.warn(this, "%s", ex);
            }
            return "";
        }

        @Override
        public byte[] binary() {
            return this.origin.binary();
        }

        @Override
        public <T extends Response> T as(Class<T> type) {
            return this.origin.as(type);
        }
    }
}
