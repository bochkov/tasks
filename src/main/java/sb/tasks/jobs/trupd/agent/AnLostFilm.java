package sb.tasks.jobs.trupd.agent;

import com.jcabi.http.Request;
import com.jcabi.http.request.JdkRequest;
import com.jcabi.log.Logger;
import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.w3c.dom.Node;
import sb.tasks.ValidProps;
import sb.tasks.jobs.meta.MetaInfo;
import sb.tasks.jobs.trupd.CurlFetch;
import sb.tasks.jobs.trupd.Filename;
import sb.tasks.jobs.trupd.TorrentResult;
import sb.tasks.jobs.trupd.TrNotif;
import sb.tasks.jobs.trupd.metafile.Metafile;

import javax.ws.rs.core.HttpHeaders;
import javax.xml.namespace.NamespaceContext;
import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class AnLostFilm extends TorrentFromPage {

    private static final String VALUE = "value";

    private final org.bson.Document document;
    private final ValidProps props;
    private final String usess;
    private final String uid;
    private final String quality;

    public AnLostFilm(org.bson.Document document, ValidProps props,
                      org.bson.Document usess, org.bson.Document uid, org.bson.Document quality) {
        this(
                document,
                props,
                usess == null ? "" : usess.getString(VALUE),
                uid == null ? "" : uid.getString(VALUE),
                quality == null ? "" : quality.getString(VALUE)
        );
    }

    public AnLostFilm(org.bson.Document document, ValidProps props,
                      String usess, String uid, String quality) {
        this.document = document;
        this.props = props;
        this.usess = usess;
        this.uid = uid;
        this.quality = quality;
    }

    @Override
    protected String name(Document document) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected String torrentUrl(Document document) {
        throw new UnsupportedOperationException();
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
                        new CurlFetch(props).fetch(url),
                        document.getString("url")
                );
                String title = root.getElementsByClass("title-ru").get(0).text();
                XML xml = new XMLDocument(
                        new JdkRequest("http://insearch.site/rssdd.xml")
                                .fetch()
                                .body()
                );
                List<String> items = xml.xpath("//item/title/text()");
                for (int i = 0; i < items.size(); ++i) {
                    String ttl = items.get(i);
                    String category = xml.xpath("//item/category/text()").get(i);
                    if (ttl.contains(title) && category.equals(String.format("[%s]", quality))) {
                        String torUrl = xml.xpath("//item/link/text()").get(i);
                        Request req = new JdkRequest(torUrl)
                                .header(HttpHeaders.COOKIE, String.format("uid=%s;usess=%s", uid, usess));
                        result.add(
                                new TorrentResult(
                                        new Metafile(req.fetch().binary()),
                                        ttl,
                                        torUrl,
                                        new Filename(props, torUrl).toFile(),
                                        url
                                )
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
