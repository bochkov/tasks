package sb.tasks.jobs.dailypress;

import com.google.common.io.Files;
import com.jcabi.http.Response;
import com.jcabi.log.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

public final class PdfFromResponse {

    private final Response response;

    public PdfFromResponse(Response response) {
        this.response = response;
    }

    public void saveTo(File out) throws IOException {
        if (response.status() == 200) {
            if ("application/pdf"
                    .equals(response
                            .headers()
                            .getOrDefault(
                                    "Content-Type",
                                    Collections.singletonList("")
                            ).get(0))) {
                Files.write(response.binary(), out);
                Logger.info(this, String.format("Downloaded file %s", out.getName()));
            } else
                Logger.info(this, "No magazine for this date");
        } else
            Logger.info(this, "No content for this page");
    }
}
