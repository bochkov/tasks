package sb.tasks.agent.trupd;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import sb.tasks.ValidProps;
import sb.tasks.agent.Agent;
import sb.tasks.jobs.trupd.TorrentResult;
import sb.tasks.models.metafile.Metafile;
import sb.tasks.service.CurlCommon;

@Slf4j
public abstract class AnTorrentFromPage implements Agent<TorrentResult> {

    protected abstract String name(Document doc) throws IOException;

    protected abstract String torrentUrl(Document doc) throws IOException;

    protected TorrentResult fromCurlReq(Document root, ValidProps props, String url) throws IOException {
        String torrentUrl = torrentUrl(root);
        var curl = new CurlCommon(props);
        return new TorrentResult(
                new Metafile(curl.binary(torrentUrl)),
                name(root),
                torrentUrl,
                new Filename(props, torrentUrl, curl.headers(torrentUrl)).toFile(),
                url
        );
    }

    @RequiredArgsConstructor
    public static final class Filename {

        private static final AtomicInteger COUNT = new AtomicInteger(1);

        private final ValidProps props;
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
            return new File(props.tmpDir(), String.format("%s.torrent", filename));
        }
    }

}
