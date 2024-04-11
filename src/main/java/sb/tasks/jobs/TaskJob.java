package sb.tasks.jobs;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import sb.tasks.model.Task;
import sb.tasks.repo.TaskRepo;
import sb.tasks.service.Agent;
import sb.tasks.service.AgentResolver;
import sb.tasks.service.jobs.JobService;
import sb.tasks.service.TaskResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;

public abstract class TaskJob implements Job {

    @Autowired
    private TaskRepo tasks;

    protected abstract Logger log();

    protected abstract AgentResolver agentResolver();

    protected abstract List<JobService<TaskResult>> services();

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        String key = context.getJobDetail().getKey().getName();
        Task task = tasks.findById(key).orElseThrow();
        log().info("Start {}", task);
        if (task.getParams() == null)
            throw new NoSuchElementException("params in task not defined");
        if (task.getVars() == null)
            task.setVars(new Task.Vars());
        try {
            log().info("Execution plan = {}", services());
            Agent agent = agentResolver().resolve(task);
            log().info("Choose agent '{}' for url={}", agent, task.getParams().getUrl());
            Collection<TaskResult> result = agent.perform(task);
            for (JobService<TaskResult> service : services()) {
                service.process(task, result);
            }
        } catch (Exception ex) {
            throw new JobExecutionException(ex);
        }
    }
}
