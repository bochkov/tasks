package sb.tasks.jobs.dailypress;

import java.io.IOException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

final class MagResultTest {

    @Test
    void test() {
        MagResult res = new MagResult(
                null,
                "http://my.url",
                "Hello world"
        );
        Assertions.assertThat(res.mailText())
                .isNotEmpty();
        Assertions.assertThat(res.mailText(new IOException("gegsdf")))
                .isNotEmpty();
        Assertions.assertThatThrownBy(res::telegramText)
                .isInstanceOf(UnsupportedOperationException.class);
    }

}