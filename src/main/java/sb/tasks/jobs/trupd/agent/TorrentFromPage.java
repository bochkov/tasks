package sb.tasks.jobs.trupd.agent;

import com.jcabi.http.request.JdkRequest;
import com.jcabi.log.Logger;
import org.jsoup.nodes.Document;
import sb.tasks.ValidProps;
import sb.tasks.agent.Agent;
import sb.tasks.jobs.trupd.CurlFetch;
import sb.tasks.jobs.trupd.Filename;
import sb.tasks.jobs.trupd.TorrentResult;
import sb.tasks.jobs.trupd.TrNotif;
import sb.tasks.jobs.trupd.metafile.Metafile;
import sb.tasks.system.net.ComboRequest;

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
        Logger.info(this, "%s", torrentUrl);
        return new TorrentResult(
                new Metafile(
                        new CurlFetch(props).binary(torrentUrl)
                ),
                name(root),
                torrentUrl,
                new Filename(props, torrentUrl).toFile(),
                url
        );
    }

}
