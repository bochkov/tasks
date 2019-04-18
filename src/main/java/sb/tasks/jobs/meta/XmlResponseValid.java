package sb.tasks.jobs.meta;

import com.jcabi.http.Request;
import com.jcabi.http.Response;

import java.util.List;
import java.util.Map;

public final class XmlResponseValid implements Response {

    private final Response origin;

    public XmlResponseValid(Response origin) {
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
        return this.origin.body()
                .replaceAll(" & ", " &amp; ");
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
