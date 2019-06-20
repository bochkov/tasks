package sb.tasks.jobs.meta;

import com.jcabi.http.request.JdkRequest;
import com.jcabi.xml.XML;
import org.cactoos.map.MapEntry;
import org.cactoos.map.MapOf;
import org.quartz.JobExecutionContext;
import sb.tasks.jobs.trupd.ComboRequest;

import java.io.IOException;
import java.util.Map;

public final class LfRss implements MetaFetch {

    @Override
    public Map<String, Object> fetch(JobExecutionContext context) throws IOException {
        XML xml = new ComboRequest(new JdkRequest("https://lostfilm.tv/rss.xml"))
                .xmlResp()
                .xml();
        return new MapOf<>(
                new MapEntry<>(
                        "rssFeed", xml
                )
        );
    }
}
