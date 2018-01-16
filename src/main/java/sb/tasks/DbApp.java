package sb.tasks;

import com.jcabi.log.Logger;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

import java.util.Properties;

public final class DbApp implements App<MongoDatabase> {

    private final Properties props;

    public DbApp(Properties props) {
        this.props = props;
    }

    @Override
    public MongoDatabase init() {
        Logger.info(this, "Communicating with database");
        return new MongoClient(
                props.getProperty("mongo.host"),
                Integer.parseInt(props.getProperty("mongo.port"))
        ).getDatabase(props.getProperty("mongo.db"));
    }
}
