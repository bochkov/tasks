package sb.tasks.system.net;

import com.jcabi.http.Response;

public final class XmlResponseValid extends CustomResponse {

    public XmlResponseValid(Response origin) {
        super(origin);
    }

    @Override
    public String body() {
        return this.response.body()
                .replaceAll(" & ", " &amp; ");
    }
}
