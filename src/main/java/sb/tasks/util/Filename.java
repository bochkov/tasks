package sb.tasks.util;

import java.io.File;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import sb.tasks.model.Property;

@Slf4j
@RequiredArgsConstructor
public final class Filename {

    private static final AtomicInteger COUNT = new AtomicInteger(1);

    private final String torrentUrl;
    private final Map<String, String> headers;

    public File toFile() {
        var filename = String.format("%d", COUNT.incrementAndGet());
        if (torrentUrl.endsWith(".torrent"))
            filename = torrentUrl.substring(torrentUrl.lastIndexOf('/'));
        else {
            String contentDisposition = headers.getOrDefault("Content-Disposition", "");
            var pattern = Pattern.compile("^attachment;\\s*filename=\"(.*)\"$");
            var matcher = pattern.matcher(contentDisposition);
            if (matcher.find()) {
                filename = matcher.group(1);
                LOG.info("Finded filename = {}", filename);
            }
        }
        return new File(Property.TMP_DIR, String.format("%s.torrent", filename));
    }
}
