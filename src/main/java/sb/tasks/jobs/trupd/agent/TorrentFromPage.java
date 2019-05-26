package sb.tasks.jobs.trupd.agent;

import com.jcabi.http.request.JdkRequest;
import org.jsoup.nodes.Document;
import sb.tasks.ValidProps;
import sb.tasks.agent.Agent;
import sb.tasks.jobs.trupd.*;
import sb.tasks.jobs.trupd.metafile.Metafile;

import java.io.IOException;

public abstract class TorrentFromPage implements Agent<TrNotif> {

    protected abstract String name(Document doc) throws IOException;

    protected abstract String torrentUrl(Document doc) throws IOException;

    protected TorrentResult fromReq(Document root, ValidProps props, String url) throws IOException {
        String torrentUrl = torrentUrl(root);
        return new TorrentResult(
                new Metafile(
                        new ComboRequest(new JdkRequest(torrentUrl))
                                .fetch()
                                .binary()
                ),
                name(root),
                torrentUrl,
                new Filename(props, torrentUrl).toFile(),
                url
        );
    }

    protected TorrentResult fromCurlReq(Document root, ValidProps props, String url) throws IOException {
        String torrentUrl = torrentUrl(root);
        return new TorrentResult(
                new Metafile(
                        new CurlFetch(props).binary(url)
                ),
                name(root),
                torrentUrl,
                new Filename(props, torrentUrl).toFile(),
                url
        );
    }

}
