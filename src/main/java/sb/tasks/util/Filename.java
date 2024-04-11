package sb.tasks.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import sb.tasks.model.Property;

import java.io.File;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@RequiredArgsConstructor
public final class Filename {

    private static final AtomicInteger COUNT = new AtomicInteger(1);

    private final String torrentUrl;
    private final Map<String, String> headers;

    public File toFile() {
        String filename = String.format("%d", COUNT.incrementAndGet());
        if (torrentUrl.endsWith(".torrent"))
            filename = torrentUrl.substring(torrentUrl.lastIndexOf('/'));
        else {
            String contentDisposition = headers.getOrDefault("Content-Disposition", "");
            Pattern pattern = Pattern.compile("^attachment;\\s*filename=\"(.*)\"$");
            Matcher matcher = pattern.matcher(contentDisposition);
            if (matcher.find()) {
                filename = matcher.group(1);
                LOG.info("Found filename = {}", filename);
            }
        }
        return new File(Property.TMP_DIR, String.format("%s.torrent", filename));
    }
}
