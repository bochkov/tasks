package sb.tasks;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class DbApp implements App<MongoDatabase> {

    private final ValidProps props;
    private final MongoClient mongo;

    public DbApp(ValidProps props) {
        this(
                props,
                MongoClients.create(String.format("mongodb://%s:%s", props.mongoHost(), props.mongoPort()))
        );
    }

    @Override
    public MongoDatabase init() {
        return mongo.getDatabase(props.mongoDb());
    }
}
