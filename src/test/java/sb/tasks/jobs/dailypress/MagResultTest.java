package sb.tasks.jobs.dailypress;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class MagResultTest {

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    private MagResult res;

    @Before
    public void setUp() throws IOException {
        res = new MagResult(
                tmpFolder.newFile(),
                "http://my.url",
                "Hello world"
        );
    }

    @Test
    public void testMailText() {
        Assert.assertFalse(res.mailText().isEmpty());
    }

    @Test
    public void testMailFailText() {
        Assert.assertFalse(res.mailText(new IOException("gegsdf")).isEmpty());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testTelegramText() {
        res.telegramText();
    }

}