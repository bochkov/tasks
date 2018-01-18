package sb.tasks.jobs.trupd;

import com.jcabi.http.request.JdkRequest;
import com.jcabi.http.response.JsoupResponse;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import sb.tasks.jobs.Agent;
import sb.tasks.jobs.trupd.metafile.Metafile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AnRutor implements Agent<TrNotif> {

    private static final Pattern[] LINK_PATTERNS = new Pattern[]{
            Pattern.compile("http://d.rutor.org/download/\\d+"),
            Pattern.compile("/parse/d.rutor.org/download/\\d+"),
            Pattern.compile("/download/\\d+")
    };

    private final org.bson.Document document;

    public AnRutor(org.bson.Document document) {
        this.document = document;
    }

    private String name(Document root) {
        Matcher m = Pattern
                .compile(".*?::(?<name>.*)")
                .matcher(
                        root.getElementsByTag("title").text());
        return m.find() ?
                m.group("name").trim() :
                "";
    }

    private String torrentUrl(Document root) throws IOException {
        Matcher matcher;
        if (root.getElementById("download") != null) {
            for (Element element : root.getElementById("download").children()) {
                for (Pattern linkPattern : LINK_PATTERNS) {
                    matcher = linkPattern.matcher(element.attr("href"));
                    if (matcher.find()) {
                        String link = matcher.group();
                        if (link.startsWith("/")) {
                            Matcher m = Pattern
                                    .compile("(?<domain>https?://.*?)/.*")
                                    .matcher(root.location());
                            if (m.find())
                                link = String.format("%s%s", m.group("domain"), link);
                        }
                        return link;
                    }
                }
            }
        }
        throw new IOException("Download section not found");
    }

    @Override
    public List<TrNotif> perform() throws IOException {
        Document root = Jsoup.parse(
                new LfRequest(new JdkRequest(document.getString("url")))
                        .fetch()
                        .as(JsoupResponse.class)
                        .body(),
                document.getString("url")
        );
        String torrentUrl = torrentUrl(root);
        return Collections.singletonList(
                new TorrentResult(
                        new Metafile(
                                new LfRequest(new JdkRequest(torrentUrl))
                                        .fetch()
                                        .binary()
                        ),
                        name(root),
                        torrentUrl,
                        new Filename(torrentUrl).toFile(),
                        document.getString("url")
                )
        );
    }
}
