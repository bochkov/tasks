package sb.tasks.system.net;

import com.jcabi.http.*;
import com.jcabi.http.response.XmlResponse;
import com.jcabi.http.wire.AutoRedirectingWire;
import com.jcabi.http.wire.RetryWire;
import com.jcabi.http.wire.TrustedWire;

import java.io.IOException;
import java.io.InputStream;

public final class ComboRequest implements Request {

    private final Request origin;

    public ComboRequest(Request origin) {
        this.origin = origin;
    }

    @Override
    public RequestURI uri() {
        return this.origin.uri();
    }

    @Override
    public RequestBody body() {
        return this.origin.body();
    }

    @Override
    public RequestBody multipartBody() {
        return this.origin.multipartBody();
    }

    @Override
    public Request header(String name, Object value) {
        return this.origin.header(name, value);
    }

    @Override
    public Request reset(String name) {
        return this.origin.reset(name);
    }

    @Override
    public Request method(String method) {
        return this.origin.method(method);
    }

    @Override
    public Request timeout(int connect, int read) {
        return this.origin.timeout(connect, read);
    }

    @Override
    public Response fetch() throws IOException {
        return this.origin
                .through(TrustedWire.class)
                .through(RetryWire.class)
                .through(AutoRedirectingWire.class)
                .fetch();
    }

    public XmlResponse xmlResp() throws IOException {
        return new XmlResponse(
                new XmlResponseValid(
                        fetch()
                )
        );
    }

    @Override
    public Response fetch(InputStream stream) throws IOException {
        return this.origin.fetch(stream);
    }

    @Override
    public <T extends Wire> Request through(Class<T> type, Object... args) {
        return this.origin.through(type, args);
    }
}
