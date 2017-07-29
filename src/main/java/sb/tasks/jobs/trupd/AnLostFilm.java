package sb.tasks.jobs.trupd;

import com.jcabi.http.request.JdkRequest;
import com.jcabi.http.wire.AutoRedirectingWire;
import com.jcabi.http.wire.CookieOptimizingWire;
import com.jcabi.http.wire.RetryWire;
import com.jcabi.http.wire.TrustedWire;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import sb.tasks.jobs.Agent;
import sb.tasks.jobs.AgentException;
import sb.tasks.jobs.trupd.metafile.Metafile;
import sb.tasks.jobs.trupd.metafile.Mt;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AnLostFilm implements Agent<TorrentResult> {

    private final org.bson.Document document;
    private final String session;
    private final String uid;
    private final String quality;

    public AnLostFilm(org.bson.Document document, String session, String uid, String quality) {
        this.document = document;
        this.session = session;
        this.uid = uid;
        this.quality = quality;
    }

    private String name(Document document) throws IOException {
        for (Element item : document.getElementsByClass("inner-box--item")) {
            if (item.getElementsByClass("inner-box--label").get(0).text().equals(quality)) {
                return item
                        .getElementsByClass("main").get(0)
                        .getElementsByTag("a").get(0)
                        .text();
            }
        }
        throw new IOException("Name not parsed");
    }

    private String torrentUrl(Document document) throws IOException {
        for (Element item : document.getElementsByClass("inner-box--item")) {
            if (item.getElementsByClass("inner-box--label").get(0).text().equals(quality)) {
                return item
                        .getElementsByClass("main").get(0)
                        .getElementsByTag("a").get(0)
                        .attr("href");
            }
        }
        throw new IOException("URL not found");
    }

    @Override
    public List<TorrentResult> perform() throws AgentException, IOException {
        Document root = Jsoup.parse(
                new JdkRequest(String.format("%s/seasons/", document.getString("url")))
                        .through(TrustedWire.class)
                        .through(AutoRedirectingWire.class)
                        .through(RetryWire.class)
                        .fetch()
                        .body(),
                document.getString("url")
        );
        Elements tables = root.getElementsByTag("table");
        for (Element table : tables) {
            for (Element row : table.getElementsByTag("tr")) {
                if (!row.hasClass("not-available")) {
                    String onclick = row.getElementsByClass("external-btn").attr("onclick");
                    Matcher m = Pattern
                            .compile("PlayEpisode\\('(\\d+)','(\\d+)','(\\d+)'\\)")
                            .matcher(onclick);
                    if (m.find()) {
                        Document doc = Jsoup.parse(
                                new JdkRequest(
                                        String.format("https://www.lostfilm.tv/v_search.php?c=%s&s=%s&e=%s",
                                                m.group(1), m.group(2), m.group(3)))
                                        .through(TrustedWire.class)
                                        .through(AutoRedirectingWire.class)
                                        .through(RetryWire.class)
                                        .through(CookieOptimizingWire.class)
                                        .header("Cookie", String.format("lf_session=%s", session))
                                        .header("Cookie", String.format("lnk_uid=%s", uid))
                                        .fetch()
                                        .body()
                        );
                        if (!doc.getElementsByTag("a").isEmpty()) {
                            org.jsoup.nodes.Document doc2 = Jsoup.parse(
                                    new JdkRequest(doc.getElementsByTag("a").get(0).attr("href"))
                                            .through(TrustedWire.class)
                                            .through(AutoRedirectingWire.class)
                                            .through(RetryWire.class)
                                            .fetch()
                                            .body()
                            );
                            String torrentUrl = torrentUrl(doc2);
                            Mt mt = new Metafile(
                                    new JdkRequest(torrentUrl)
                                            .through(TrustedWire.class)
                                            .through(AutoRedirectingWire.class)
                                            .through(RetryWire.class)
                                            .fetch()
                                            .binary()
                            );
                            return Collections.singletonList(
                                    new TorrentResult(mt, name(doc2), torrentUrl,
                                            new Filename(
                                                    document.getString("download_dir"),
                                                    torrentUrl
                                            ).toFile())
                            );
                        }
                        throw new IOException("Document not found");
                    }
                }
            }
        }
        throw new IOException("Document not found");
    }
}
