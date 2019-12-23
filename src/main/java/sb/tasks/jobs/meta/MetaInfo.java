package sb.tasks.jobs.meta;

import com.jcabi.log.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class MetaInfo implements Job {

    private static final Map<String, Object> META = new ConcurrentHashMap<>();
    private static final List<MetaFetch> FETCH = Collections.singletonList(
            new LfRss()
    );

    public static <T> T get(String key, Class<T> clz, T def) {
        return META.containsKey(key) ?
                clz.cast(META.get(key)) :
                def;
    }

    @Override
    public void execute(JobExecutionContext context) {
        for (MetaFetch fetch : FETCH) {
            try {
                META.putAll(
                        fetch.fetch(context)
                );
            } catch (Exception ex) {
                Logger.warn(this, "Cannot fetch metadata %s\n%s", fetch, ex);
            }
        }
        Logger.info(this, "MetaInfo execution completed");
    }
}
