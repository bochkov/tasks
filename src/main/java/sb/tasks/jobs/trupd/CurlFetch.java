package sb.tasks.jobs.trupd;

import com.google.common.io.ByteStreams;
import com.jcabi.immutable.Array;
import com.jcabi.log.Logger;
import org.cactoos.list.Joined;
import org.cactoos.list.ListOf;
import sb.tasks.ValidProps;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class CurlFetch {

    private final ValidProps props;

    public CurlFetch(ValidProps props) {
        this.props = props;
    }

    public String fetch(String url) throws IOException {
        return fetch0(downloadCmd(url));
    }

    public String fetch(String url, Array<Map.Entry<String, String>> headers) throws IOException {
        return fetch0(downloadCmd(url, headers));
    }

    private String fetch0(List<String> url) throws IOException {
        Logger.info(this, "download cmd = %s", url);
        Process pp = new ProcessBuilder(url)
                .redirectInput(ProcessBuilder.Redirect.PIPE)
                .start();
        StringBuilder res = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(pp.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null)
                res.append(line).append("\n");
        }
        return res.toString();
    }

    public byte[] binary(String url) throws IOException {
        Logger.info(this, "Downloading from url %s", url);
        Process pp = new ProcessBuilder(downloadCmd(url))
                .redirectInput(ProcessBuilder.Redirect.PIPE)
                .start();
        return ByteStreams.toByteArray(pp.getInputStream());
    }

    private List<String> downloadCmd(String url) {
        return new ListOf<>(
                new Joined<>(
                        new ListOf<>(
                                "/usr/bin/curl",
                                url,
                                "-L" // follow redirects
                        ),
                        props.curlExtraAsList()
                )
        );
    }

    private List<String> downloadCmd(String url, Array<Map.Entry<String, String>> adds) {
        List<String> headers = new ArrayList<>();
        for (Map.Entry<String, String> add : adds) {
            headers.add("-H");
            headers.add(String.format("\"%s: %s\"", add.getKey(), add.getValue()));
        }
        return new ListOf<>(
                new Joined<>(
                        new ListOf<>(
                                "/usr/bin/curl",
                                url,
                                "-L" // follow redirects
                        ),
                        props.curlExtraAsList(),
                        headers
                )
        );
    }
}
