package sb.tasks.jobs.trupd;

import com.jcabi.http.request.JdkRequest;
import com.jcabi.log.Logger;
import sb.tasks.ValidProps;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Filename {

    private static final AtomicInteger COUNT = new AtomicInteger(1);

    private final ValidProps props;
    private final String torrentUrl;

    public Filename(ValidProps props, String torrentUrl) {
        this.props = props;
        this.torrentUrl = torrentUrl;
    }

    public File toFile() throws IOException {
        String filename = String.format("%d", COUNT.incrementAndGet());
        if (torrentUrl.endsWith(".torrent"))
            filename = torrentUrl.substring(torrentUrl.lastIndexOf('/'));
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
                props.tmpDir(),
                String.format("%s.torrent", filename)
        );
    }
}
