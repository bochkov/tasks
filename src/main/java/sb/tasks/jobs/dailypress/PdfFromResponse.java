package sb.tasks.jobs.dailypress;

import com.jcabi.http.Response;
import com.jcabi.log.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;

public final class PdfFromResponse {

    private final Response response;

    public PdfFromResponse(Response response) {
        this.response = response;
    }

    public void saveTo(File out) {
        if (response.status() == 200) {
            String hdr = response.headers()
                    .getOrDefault("Content-Type", Collections.singletonList(""))
                    .get(0);
            Logger.info(this, "Received headers %s", hdr);
            if ("application/pdf".equals(hdr)) {
                try (FileOutputStream fous = new FileOutputStream(out)) {
                    fous.write(response.binary());
                    Logger.info(this, "Downloaded file %s", out.getName());
                } catch (IOException ex) {
                    Logger.info(this, "Download file %s failed", out.getName());
                }
            } else
                Logger.info(this, "No magazine for this date");
        } else
            Logger.info(this, "No content for this page");
    }
}
