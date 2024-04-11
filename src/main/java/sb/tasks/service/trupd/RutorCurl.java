package sb.tasks.service.trupd;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

@Slf4j
@Component
public final class RutorCurl {

    @Value("${curl.extra-opts}")
    private String extraOpts = "";

    public String fetch(String url) throws IOException {
        return fetch0(downloadCmd(url));
    }

    private String fetch0(List<String> cmd) throws IOException {
        LOG.info("download cmd = {}", cmd);
        Process pp = new ProcessBuilder(cmd)
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
        List<String> cmd = downloadCmd(url);
        LOG.info("Downloading from url {}, cmd={}", url, cmd);
        Process pp = new ProcessBuilder(cmd)
                .redirectInput(ProcessBuilder.Redirect.PIPE)
                .start();
        return StreamUtils.copyToByteArray(pp.getInputStream());
    }

    public Map<String, String> headers(String url) throws IOException {
        Map<String, String> headers = new HashMap<>();
        List<String> cmd = headersCmd(url);
        LOG.info("Headers from url {}", cmd);
        Process pp = new ProcessBuilder(cmd)
                .redirectInput(ProcessBuilder.Redirect.PIPE)
                .start();
        byte[] bytes = StreamUtils.copyToByteArray(pp.getInputStream());
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
        cmd.addAll(Arrays.asList(extraOpts.split("\\s+")));
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
