package sb.tasks.system.net;

import com.jcabi.http.Request;
import com.jcabi.http.Response;

import java.util.List;
import java.util.Map;

public abstract class CustomResponse implements Response {

    protected final transient Response response;

    CustomResponse(final Response resp) {
        this.response = resp;
    }

    @Override
    public final String toString() {
        return this.response.toString();
    }

    @Override
    public final Request back() {
        return this.response.back();
    }

    @Override
    public final int status() {
        return this.response.status();
    }

    @Override
    public final String reason() {
        return this.response.reason();
    }

    @Override
    public final Map<String, List<String>> headers() {
        return this.response.headers();
    }

    @Override
    public String body() {
        return this.response.body();
    }

    @Override
    public final byte[] binary() {
        return this.response.binary();
    }

    @Override
    public final <T extends Response> T as(final Class<T> type) {
        return this.response.as(type);
    }
}
