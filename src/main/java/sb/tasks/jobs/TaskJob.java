package sb.tasks.jobs;

import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import sb.tasks.model.Task;
import sb.tasks.repo.TaskRepo;
import sb.tasks.service.Agent;
import sb.tasks.service.AgentResolver;
import sb.tasks.service.TaskResult;
import sb.tasks.service.jobs.JobService;

import java.util.ArrayList;
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

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        String key = context.getJobDetail().getKey().getName();
        Task task = tasks.findById(key).orElseThrow();
        LOG.info("Start {}", task);
        if (task.getParams() == null)
            throw new NoSuchElementException("params in task not defined");
        if (task.getVars() == null)
            task.setVars(new Task.Vars());
        try {
            LOG.info("Execution plan = {}", services);
            Agent agent = agentResolver().resolve(task);
            Collection<TaskResult> result = new ArrayList<>(agent.perform(task));
            for (JobService<TaskResult> service : services) {
                service.process(task, result);
            }
        } catch (Exception ex) {
            throw new JobExecutionException(ex);
        }
    }
}
