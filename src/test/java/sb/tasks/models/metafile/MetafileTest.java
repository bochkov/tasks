package sb.tasks.models.metafile;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import sb.tasks.jobs.trupd.TorrentResultTest;

public class MetafileTest {

    private Metafile mt;

    @Before
    public void setUp() throws Exception {
        byte[] bytes;
        try (var is = TorrentResultTest.class.getResourceAsStream("/tor.torrent")) {
            Assert.assertNotNull(is);
            bytes = is.readAllBytes();
        }
        mt = new Metafile(bytes);
    }

    @Test
    public void testName() {
        Assert.assertEquals("Михайлов В.С. и др. - Растительно-молочно-яичные блюда - 1982.pdf", mt.name());
    }

    @Test
    public void testCreationDate() {
        Calendar cal = new GregorianCalendar(2021, Calendar.JUNE, 10, 15, 59, 24);
        Assert.assertEquals(cal.getTime(), mt.creationDate());
    }
}