package sb.tasks.repo;

import org.springframework.data.mongodb.repository.MongoRepository;
import sb.tasks.model.Property;

public interface PropertyRepo extends MongoRepository<Property, String> {
}
