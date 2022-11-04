package sb.tasks.repo;

import org.springframework.data.mongodb.repository.MongoRepository;
import sb.tasks.model.Task;

public interface TaskRepo extends MongoRepository<Task, String> {
}
