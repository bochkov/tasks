package sb.tasks.entity;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface PropertyRepo extends MongoRepository<Property, String> {

    default String userAgent() {
        return findById(Property.HTTP_USER_AGENT_KEY)
                .orElse(new Property(Property.HTTP_USER_AGENT_KEY, ""))
                .getValue();
    }

}
