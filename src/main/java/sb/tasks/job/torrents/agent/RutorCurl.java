package sb.tasks.job.torrents.agent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import sb.tasks.job.torrents.util.Filename;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

@Slf4j
@Component
public final class RutorCurl {

    @Value("${curl.extra-opts}")
    private String extraOpts = "";

    public String fetch(String url) throws IOException {
        List<String> cmd = downloadCmd(url);
        LOG.info("Download cmd = {}", cmd);
        Process pp = new ProcessBuilder(cmd)
                .redirectInput(ProcessBuilder.Redirect.PIPE)
                .start();
        StringBuilder res = new StringBuilder();
        try (InputStream is = pp.getInputStream();
             InputStreamReader isRead = new InputStreamReader(is);
             BufferedReader reader = new BufferedReader(isRead)) {
            String line;
            while ((line = reader.readLine()) != null)
                res.append(line).append("\n");
        }
        return res.toString();
    }

    public File save(String url) throws IOException {
        List<String> cmd = downloadCmd(url);
        LOG.info("Downloading from url {}, cmd={}", url, cmd);
        Process pp = new ProcessBuilder(cmd)
                .redirectInput(ProcessBuilder.Redirect.PIPE)
                .start();
        byte[] bytes = StreamUtils.copyToByteArray(pp.getInputStream());
        File outFile = new Filename(url, headers(url)).toFile();
        Files.write(outFile.toPath(), bytes);
        return outFile;
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
                String[] ln = line.split(":\\s+");
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
