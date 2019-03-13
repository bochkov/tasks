package sb.tasks;

import com.jcabi.log.Logger;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

public final class DbApp implements App<MongoDatabase> {

    private final ValidProps props;

    public DbApp(ValidProps props) {
        this.props = props;
    }

    @Override
    public MongoDatabase init() {
        Logger.info(this, "Communicating with database");
        String host = props.mongoHost();
        int port = props.mongoPort();
        try (MongoClient client = new MongoClient(host, port)) {
            return client.getDatabase(props.mongoDb());
        }
    }
}
