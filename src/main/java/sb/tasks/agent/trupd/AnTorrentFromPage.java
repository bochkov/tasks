package sb.tasks.agent.trupd;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import kong.unirest.Unirest;
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

    protected TorrentResult fromReq(Document root, ValidProps props, String url) throws IOException {
        String torrentUrl = torrentUrl(root);
        var body = Unirest.get(torrentUrl)
                .asBytes()
                .getBody();
        return new TorrentResult(
                new Metafile(body),
                name(root),
                torrentUrl,
                new Filename(props, torrentUrl).toFile(),
                url
        );
    }

    protected TorrentResult fromCurlReq(Document root, ValidProps props, String url) throws IOException {
        String torrentUrl = torrentUrl(root);
        LOG.info(torrentUrl);
        return new TorrentResult(
                new Metafile(
                        new CurlCommon(props).binary(torrentUrl)
                ),
                name(root),
                torrentUrl,
                new Filename(props, torrentUrl).toFile(),
                url
        );
    }

    @RequiredArgsConstructor
    public static final class Filename {

        private static final AtomicInteger COUNT = new AtomicInteger(1);

        private final ValidProps props;
        private final String torrentUrl;

        public File toFile() {
            var filename = String.format("%d", COUNT.incrementAndGet());
            if (torrentUrl.endsWith(".torrent"))
                filename = torrentUrl.substring(torrentUrl.lastIndexOf('/'));
            else {
                List<String> headers = Unirest.get(torrentUrl)
                        .asEmpty()
                        .getHeaders().get("Content-Disposition");
                var pattern = Pattern.compile("^attachment;\\s*filename=\"(.*)\"$");
                for (String header : headers) {
                    LOG.info("Find header={}", header);
                    var matcher = pattern.matcher(header);
                    if (matcher.find())
                        filename = matcher.group(1);
                }
            }
            return new File(props.tmpDir(), String.format("%s.torrent", filename));
        }
    }

}
