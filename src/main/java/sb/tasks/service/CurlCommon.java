package sb.tasks.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import com.google.common.io.ByteStreams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import sb.tasks.ValidProps;

@Slf4j
@RequiredArgsConstructor
public final class CurlCommon {

    private final ValidProps props;

    public String fetch(String url) throws IOException {
        return fetch0(downloadCmd(url));
    }

    private String fetch0(List<String> url) throws IOException {
        LOG.info("download cmd = {}", url);
        Process pp = new ProcessBuilder(url)
                .redirectInput(ProcessBuilder.Redirect.PIPE)
                .start();
        var res = new StringBuilder();
        try (var reader = new BufferedReader(new InputStreamReader(pp.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null)
                res.append(line).append("\n");
        }
        return res.toString();
    }

    public byte[] binary(String url) throws IOException {
        LOG.info("Downloading from url {}", url);
        Process pp = new ProcessBuilder(downloadCmd(url))
                .redirectInput(ProcessBuilder.Redirect.PIPE)
                .start();
        return ByteStreams.toByteArray(pp.getInputStream());
    }

    private List<String> downloadCmd(String url) {
        return List.of("/usr/bin/curl", url, "-L", props.curlExtra());
    }
}
