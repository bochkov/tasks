package sb.tasks.models.metafile;

import java.io.IOException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class MetafileTest {

    @Test
    void testMetafileParse() throws IOException {
        byte[] bytes;
        try (var is = this.getClass().getResourceAsStream("/tor.torrent")) {
            Assertions.assertThat(is).isNotNull();
            bytes = is.readAllBytes();
        }
        Metafile mt = new Metafile(bytes);
        Assertions.assertThat(mt.name()).isEqualTo("Михайлов В.С. и др. - Растительно-молочно-яичные блюда - 1982.pdf");
        Assertions.assertThat(mt.creationDate()).isNotNull();
    }
}