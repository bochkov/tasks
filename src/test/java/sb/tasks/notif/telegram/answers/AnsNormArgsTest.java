package sb.tasks.notif.telegram.answers;

import com.mongodb.client.MongoDatabase;
import org.junit.Assert;
import org.junit.Test;
import sb.tasks.notif.telegram.TgAnsFactory;

public class AnsNormArgsTest {

    @Test
    public void testNorm() {
        AnsNormArgs ans = new AnsNormArgs(
                new Answer() {
                    @Override
                    public void handle(String chatId, String[] args) {
                        Assert.assertArrayEquals(
                                new String[]{
                                        "https://lostfilm.tv/series/Altered_Carbon/",
                                        "1", "2", "3"
                                },
                                args
                        );
                        Assert.assertEquals("123", chatId);
                    }

                    @Override
                    public MongoDatabase db() {
                        throw new IllegalStateException();
                    }

                    @Override
                    public TgAnsFactory ansFactory() {
                        return null;
                    }
                }
        );
        ans.handle("123", new String[]{
                "http://www.lostfilm.tv/series/Altered_Carbon/",
                "1", "2", "3"
        });
        ans.handle("123", new String[]{
                "https://www.lostfilm.tv/series/Altered_Carbon/",
                "1", "2", "3"
        });
        ans.handle("123", new String[]{
                "https://lostfilm.tv/series/Altered_Carbon/",
                "1", "2", "3"
        });
    }

    @Test
    public void testNormNotMatch() {
        AnsNormArgs ans = new AnsNormArgs(
                new Answer() {
                    @Override
                    public void handle(String chatId, String[] args) {
                        Assert.assertArrayEquals(
                                new String[]{"http://anti-tor.org/1233123/"},
                                args
                        );
                        Assert.assertEquals("234", chatId);
                    }

                    @Override
                    public MongoDatabase db() {
                        throw new IllegalStateException();
                    }

                    @Override
                    public TgAnsFactory ansFactory() {
                        return null;
                    }
                }
        );
        ans.handle("234", new String[]{"http://anti-tor.org/1233123/"});
    }
}