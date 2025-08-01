package sb.tasks.models.metafile;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import sb.tasks.job.torrents.util.Metafile;

import java.io.IOException;
import java.time.LocalDateTime;

class MetafileTest {

    @Test
    void testMetafileParse() throws IOException {
        byte[] bytes;
        try (var is = this.getClass().getResourceAsStream("/tor.torrent")) {
            Assertions.assertThat(is).isNotNull();
            bytes = is.readAllBytes();
        }
        Metafile mt = new Metafile(bytes);
        mt.parse();
        Assertions.assertThat(mt.name()).isEqualTo("Михайлов В.С. и др. - Растительно-молочно-яичные блюда - 1982.pdf");
        Assertions.assertThat(mt.creationDate()).isEqualTo(LocalDateTime.of(2021, 6, 10, 15, 59, 24));
    }
}