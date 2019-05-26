package sb.tasks.jobs.trupd;

import org.cactoos.collection.Joined;
import org.cactoos.list.ListOf;
import sb.tasks.ValidProps;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public final class CurlFetch {

    private final ValidProps props;

    public CurlFetch(ValidProps props) {
        this.props = props;
    }

    public String fetch(String url) throws IOException {
        Process pp = new ProcessBuilder(downloadCmd(url))
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

    private List<String> downloadCmd(String url) {
        return new ListOf<>(
                new Joined<>(
                        new ListOf<>(
                                "/usr/bin/curl",
                                url
                        ),
                        props.curlExtraAsList()
                )
        );
    }
}
