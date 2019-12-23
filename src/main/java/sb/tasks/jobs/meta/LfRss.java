package sb.tasks.jobs.meta;

import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import org.cactoos.map.MapEntry;
import org.cactoos.map.MapOf;
import org.quartz.JobExecutionContext;
import sb.tasks.ValidProps;
import sb.tasks.jobs.trupd.CurlFetch;

import java.io.IOException;
import java.util.Map;

public final class LfRss implements MetaFetch {

    @Override
    public Map<String, Object> fetch(JobExecutionContext context) throws IOException {
        ValidProps props = (ValidProps) context.getMergedJobDataMap().get("properties");
        XML xml = new XMLDocument(
                new CurlFetch(props).fetch("https://lostfilm.tv/rss.xml")
        );
        return new MapOf<>(
                new MapEntry<>(
                        "rssFeed", xml
                )
        );
    }
}
