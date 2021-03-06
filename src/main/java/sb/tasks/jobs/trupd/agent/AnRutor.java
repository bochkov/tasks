package sb.tasks.jobs.trupd.agent;

import com.jcabi.log.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import sb.tasks.ValidProps;
import sb.tasks.jobs.trupd.CurlFetch;
import sb.tasks.jobs.trupd.TrNotif;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AnRutor extends TorrentFromPage {

    private static final Pattern[] LINK_PATTERNS = new Pattern[]{
            Pattern.compile("http://d.rutor.info/download/\\d+"),
            Pattern.compile("/parse/d.rutor.info/download/\\d+"),
            Pattern.compile("/download/\\d+"),
    };

    private final org.bson.Document document;
    private final ValidProps props;

    public AnRutor(org.bson.Document document, ValidProps props) {
        this.document = document;
        this.props = props;
    }

    @Override
    protected String name(Document root) {
        Matcher m = Pattern
                .compile(".*?::(?<name>.*)")
                .matcher(
                        root.getElementsByTag("title").text());
        return m.find() ?
                m.group("name").trim() :
                "";
    }

    @Override
    protected String torrentUrl(Document root) throws IOException {
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
                        Logger.info(this, "Found download link %s", link);
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
                new CurlFetch(props)
                        .fetch(document.getString("url"))
        );
        return Collections.singletonList(
                fromCurlReq(root, props, document.getString("url"))
        );
    }
}
