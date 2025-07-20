package sb.tasks.job.torrents.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import sb.tasks.entity.Property;
import sb.tasks.util.ContentName;

import java.io.File;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@RequiredArgsConstructor
public final class Filename {

    private static final AtomicInteger COUNT = new AtomicInteger(1);

    private final String torrentUrl;
    private final Map<String, String> headers;

    public File toFile() {
        String filename = String.format("%d.torrent", COUNT.incrementAndGet());
        if (torrentUrl.endsWith(".torrent")) {
            filename = torrentUrl.substring(torrentUrl.lastIndexOf('/'));
        } else {
            String cd = headers.getOrDefault(HttpHeaders.CONTENT_DISPOSITION, "");
            String fn = new ContentName(cd).get();
            if (fn != null && !fn.isEmpty()) {
                filename = fn;
                LOG.info("Found filename = {}", filename);
            }
        }
        return new File(Property.TMP_DIR, filename);
    }
}
