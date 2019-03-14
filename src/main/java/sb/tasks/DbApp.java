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
    @SuppressWarnings("squid:S2095")
    public MongoDatabase init() {
        Logger.info(this, "Communicating with database %s:%s/%s",
                props.mongoHost(), props.mongoPort(), props.mongoDb());
        return new MongoClient(props.mongoHost(), props.mongoPort())
                .getDatabase(props.mongoDb());
    }
}
