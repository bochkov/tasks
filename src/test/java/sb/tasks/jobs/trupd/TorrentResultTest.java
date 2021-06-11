package sb.tasks.jobs.trupd;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import sb.tasks.models.metafile.Metafile;

public final class TorrentResultTest {

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    private TorrentResult res;

    @Before
    public void setUp() throws IOException {
        byte[] bytes;
        try (var is = TorrentResultTest.class.getResourceAsStream("/tor.torrent")) {
            Assert.assertNotNull(is);
            bytes = is.readAllBytes();
        }
        res = new TorrentResult(
                new Metafile(bytes),
                "title",
                "http://download.url",
                tmpFolder.newFile(),
                "http://base.url"
        );
    }

    @Test
    public void testMailText() {
        Assert.assertFalse(res.mailText().isEmpty());
    }

    @Test
    public void testMailFailText() {
        Assert.assertFalse(res.mailText(new IOException("fsdf")).isEmpty());
    }

    @Test
    public void testTelegramText() {
        Assert.assertFalse(res.telegramText().isEmpty());
    }

}