package sb.tasks.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

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

    private String fetch0(List<String> cmd) throws IOException {
        LOG.info("download cmd = {}", cmd);
        Process pp = new ProcessBuilder(cmd)
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
        List<String> cmd = downloadCmd(url);
        LOG.info("Downloading from url {}, cmd={}", url, cmd);
        Process pp = new ProcessBuilder(cmd)
                .redirectInput(ProcessBuilder.Redirect.PIPE)
                .start();
        return ByteStreams.toByteArray(pp.getInputStream());
    }

    public Map<String, String> headers(String url) throws IOException {
        Map<String, String> headers = new HashMap<>();
        List<String> cmd = headersCmd(url);
        LOG.info("Headers from url {}", cmd);
        Process pp = new ProcessBuilder(cmd)
                .redirectInput(ProcessBuilder.Redirect.PIPE)
                .start();
        byte[] bytes = ByteStreams.toByteArray(pp.getInputStream());
        for (String line : new String(bytes).split("\n")) {
            if (line.contains(":")) {
                String[] ln = line.split(":\s+");
                if (ln.length == 2) {
                    headers.put(ln[0], ln[1]);
                }
            }
        }
        return headers;
    }

    private List<String> downloadCmd(String url) {
        List<String> cmd = new ArrayList<>();
        cmd.add("curl");
        cmd.add(url);
        cmd.add("-L");
        cmd.addAll(Arrays.asList(props.curlExtra().split("\\s+")));
        return cmd;
    }

    private List<String> headersCmd(String url) {
        List<String> cmd = downloadCmd(url);
        cmd.add("-D");
        cmd.add("-");
        cmd.add("-o");
        cmd.add("/dev/null");
        return cmd;
    }
}
