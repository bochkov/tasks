package sb.tasks.system.net;

import com.jcabi.http.Response;
import com.jcabi.log.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

public final class GzipResponse extends CustomResponse {

    public GzipResponse(Response origin) {
        super(origin);
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
}
