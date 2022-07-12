package sb.tasks.jobs.trupd;

import java.io.IOException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

final class TorrentResultTest {

    @Test
    void test() {
        TorrentResult res = new TorrentResult(
                null,
                "title",
                "http://download.url",
                null,
                "http://base.url"
        );

        Assertions.assertThat(res.mailText())
                .isNotEmpty();
        Assertions.assertThat(res.mailText(new IOException("fsdf")))
                .isNotEmpty();
        Assertions.assertThat(res.telegramText())
                .isNotEmpty();
    }

}