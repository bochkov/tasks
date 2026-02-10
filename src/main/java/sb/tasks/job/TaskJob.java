package sb.tasks.job;

import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import sb.tasks.entity.Task;
import sb.tasks.entity.TaskRepo;
import sb.tasks.service.jobs.JobService;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
public abstract class TaskJob implements Job {

    @Autowired
    private TaskRepo tasks;
    @Autowired
    private List<JobService<TaskResult>> services;

    protected abstract AgentResolver agentResolver();

    private Collection<TaskResult> perform(Agent agent, Task task) throws IOException {
        LOG.debug("Invoke 'beforePerform' of {}", task);
        agent.beforePerform();
        try {
            LOG.debug("Invoke 'perform' of {}", task);
            return agent.perform(task);
        } finally {
            LOG.debug("Invoke 'afterPerform' of {}", task);
            agent.afterPerform();
        }
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        String key = context.getJobDetail().getKey().getName();
        Task task = tasks.findById(key).orElseThrow();
        if (task.getParams() == null) {
            throw new NoSuchElementException("Params not defined");
        }
        if (task.getVars() == null) {
            task.setVars(new Task.Vars());
        }
        LOG.info("Start {}, execution plan = {}", task, services);
        Agent agent = agentResolver().resolve(task);
        try {
            Collection<TaskResult> result = perform(agent, task);
            for (JobService<TaskResult> service : services) {
                service.process(task, result);
            }
        } catch (UpdatesNotFound _) {
            //
        } catch (Exception ex) {
            throw new JobExecutionException(ex);
        }
    }
}
