package sb.tasks.jobs.trupd.agent;

import com.jcabi.http.request.JdkRequest;
import com.jcabi.http.response.JsoupResponse;
import com.jcabi.http.wire.CookieOptimizingWire;
import com.jcabi.log.Logger;
import com.jcabi.xml.XML;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.w3c.dom.Node;
import sb.tasks.ValidProps;
import sb.tasks.jobs.meta.MetaInfo;
import sb.tasks.jobs.trupd.TrNotif;
import sb.tasks.system.net.ComboRequest;

import javax.xml.namespace.NamespaceContext;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AnLostFilm extends TorrentFromPage {

    private static final String VALUE = "value";

    private final org.bson.Document document;
    private final ValidProps props;
    private final String session;
    private final String uid;
    private final String quality;

    public AnLostFilm(org.bson.Document document, ValidProps props,
                      org.bson.Document session, org.bson.Document uid, org.bson.Document quality) {
        this(
                document,
                props,
                session == null ? "" : session.getString(VALUE),
                uid == null ? "" : uid.getString(VALUE),
                quality == null ? "" : quality.getString(VALUE)
        );
    }

    public AnLostFilm(org.bson.Document document, ValidProps props,
                      String session, String uid, String quality) {
        this.document = document;
        this.props = props;
        this.session = session;
        this.uid = uid;
        this.quality = quality;
    }

    @Override
    protected String name(Document document) throws IOException {
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

    @Override
    protected String torrentUrl(Document document) throws IOException {
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
        XML rss = MetaInfo.get("rssFeed", XML.class, new EmptyFeed());
        List<TrNotif> result = new ArrayList<>();
        for (String str : rss.xpath("//item/link/text()")) {
            String decodedUrl = URLDecoder.decode(document.getString("url"), StandardCharsets.UTF_8);
            if (str.startsWith(decodedUrl)) {
                String url = str.replaceAll("\\s", "%20");
                Document root = Jsoup.parse(
                        new ComboRequest(new JdkRequest(url)).fetch().as(JsoupResponse.class).body(),
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
                        result.add(
                                fromReq(doc2, props, document.getString("url"))
                        );
                    }
                }
            }
        }

        return result.isEmpty() ?
                Collections.singletonList(
                        new TrNotif.CheckedNotif(document, props.isInitial())
                ) :
                result;
    }

    private final class EmptyFeed implements XML {

        @Override
        public List<String> xpath(String query) {
            Logger.info(AnLostFilm.this, "rss is empty");
            return Collections.emptyList();
        }

        @Override
        public List<XML> nodes(String query) {
            return Collections.emptyList();
        }

        @Override
        public XML registerNs(String prefix, Object uri) {
            return this;
        }

        @Override
        public XML merge(NamespaceContext context) {
            return this;
        }

        @Override
        public Node node() {
            return null;
        }
    }
}
