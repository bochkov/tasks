package sb.tasks.jobs.trupd;

import com.jcabi.http.request.JdkRequest;
import com.jcabi.log.Logger;
import org.bson.Document;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Filename {

    private final Document document;
    private final String torrentUrl;

    public Filename(Document document, String torrentUrl) {
        this.document = document;
        this.torrentUrl = torrentUrl;
    }

    public File toFile() throws IOException {
        String filename = "default";
        if (torrentUrl.endsWith(".torrent"))
            filename = torrentUrl.substring(torrentUrl.lastIndexOf("/"));
        else {
            List<String> headers = new JdkRequest(torrentUrl)
                    .fetch()
                    .headers()
                    .getOrDefault("Content-Disposition", Collections.emptyList());
            Pattern pattern = Pattern.compile("^attachment;\\s*filename=\"(.*)\"$");
            for (String header : headers) {
                Logger.info(this, "Find header=%s", header);
                Matcher matcher = pattern.matcher(header);
                if (matcher.find())
                    filename = matcher.group(1);
            }
        }
        return new File(
                this.document.get("download_dir", System.getProperty("user.dir")),
                filename
        );
    }
}
