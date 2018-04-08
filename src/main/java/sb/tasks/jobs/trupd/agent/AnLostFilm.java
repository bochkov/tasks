package sb.tasks.jobs.trupd.agent;

import com.jcabi.http.request.JdkRequest;
import com.jcabi.http.response.JsoupResponse;
import com.jcabi.http.response.XmlResponse;
import com.jcabi.http.wire.CookieOptimizingWire;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import sb.tasks.agent.Agent;
import sb.tasks.jobs.trupd.ComboRequest;
import sb.tasks.jobs.trupd.Filename;
import sb.tasks.jobs.trupd.TorrentResult;
import sb.tasks.jobs.trupd.TrNotif;
import sb.tasks.jobs.trupd.metafile.Metafile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AnLostFilm implements Agent<TrNotif> {

    private final org.bson.Document document;
    private final Properties props;
    private final String session;
    private final String uid;
    private final String quality;

    public AnLostFilm(org.bson.Document document, Properties props,
                      String session, String uid, String quality) {
        this.document = document;
        this.props = props;
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
    public List<TrNotif> perform() throws IOException {
        List<String> items = new ComboRequest(new JdkRequest("http://lostfilm.tv/rss.xml"))
                .fetch()
                .as(XmlResponse.class)
                .xml()
                .xpath("//item/link/text()");
        for (String str : items) {
            if (str.startsWith(document.getString("url"))) {
                Document root = Jsoup.parse(
                        new ComboRequest(new JdkRequest(str)).fetch().as(JsoupResponse.class).body(),
                        document.getString("url")
                );
                Matcher m = Pattern.compile("PlayEpisode\\('(\\d{3})(\\d{3})(\\d{3})'\\)")
                        .matcher(
                                root.getElementsByClass("external-btn").attr("onclick")
                        );
                if (m.find()) {
                    Document doc = Jsoup.parse(
                            new ComboRequest(
                                    new JdkRequest(
                                            String.format("https://lostfilm.tv/v_search.php?c=%s&s=%s&e=%s",
                                                    m.group(1), m.group(2), m.group(3))))
                                    .through(CookieOptimizingWire.class)
                                    .header("Cookie", String.format("lf_session=%s", session))
                                    .header("Cookie", String.format("lnk_uid=%s", uid))
                                    .fetch()
                                    .as(JsoupResponse.class)
                                    .body()
                    );
                    if (!doc.getElementsByTag("a").isEmpty()) {
                        org.jsoup.nodes.Document doc2 = Jsoup.parse(
                                new ComboRequest(
                                        new JdkRequest(
                                                doc.getElementsByTag("a").get(0).attr("href")
                                        )
                                ).fetch().body()
                        );
                        String torrentUrl = torrentUrl(doc2);
                        return Collections.singletonList(
                                new TorrentResult(
                                        new Metafile(
                                                new ComboRequest(new JdkRequest(torrentUrl))
                                                        .fetch()
                                                        .binary()
                                        ),
                                        name(doc2),
                                        torrentUrl,
                                        new Filename(props, torrentUrl).toFile(),
                                        document.getString("url")
                                )
                        );
                    }
                    throw new IOException("Document not found");
                }
            }
        }
        return Collections.singletonList(
                new TrNotif.CheckedNotif(document)
        );
    }
}
