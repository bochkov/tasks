package sb.tasks;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public final class DbApp implements App<MongoDatabase> {

    private final ValidProps props;
    private final MongoClient mongo;

    public DbApp(ValidProps props) {
        this.props = props;
        this.mongo = MongoClients.create(
                String.format(
                        "mongodb://%s:%s",
                        this.props.mongoHost(),
                        this.props.mongoPort()
                )
        );
    }

    @Override
    public MongoDatabase init() {
        return mongo.getDatabase(props.mongoDb());
    }
}
