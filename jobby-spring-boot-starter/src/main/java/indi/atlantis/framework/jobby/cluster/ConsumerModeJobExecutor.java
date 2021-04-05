package indi.atlantis.framework.jobby.cluster;

import java.util.Date;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import indi.atlantis.framework.jobby.DependencyType;
import indi.atlantis.framework.jobby.Job;
import indi.atlantis.framework.jobby.JobException;
import indi.atlantis.framework.jobby.JobExecutor;
import indi.atlantis.framework.jobby.JobKey;
import indi.atlantis.framework.jobby.JobLoggerFactory;
import indi.atlantis.framework.jobby.JobManager;
import indi.atlantis.framework.jobby.JobRuntimeListenerContainer;
import indi.atlantis.framework.jobby.JobTemplate;
import indi.atlantis.framework.jobby.LogManager;
import indi.atlantis.framework.jobby.RetryPolicy;
import indi.atlantis.framework.jobby.RunningState;
import indi.atlantis.framework.jobby.SerialDependencyScheduler;
import indi.atlantis.framework.jobby.StopWatch;
import indi.atlantis.framework.jobby.TraceIdGenerator;
import indi.atlantis.framework.jobby.utils.JavaMailService;

/**
 * 
 * ConsumerModeJobExecutor
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
public class ConsumerModeJobExecutor extends JobTemplate implements JobExecutor {

	@Autowired
	private SerialDependencyScheduler serialDependencyScheduler;

	@Autowired
	private StopWatch stopWatch;

	@Autowired
	private JobManager jobManager;

	@Autowired
	private LogManager logManager;

	@Autowired
	private RetryPolicy retryPolicy;

	@Autowired
	private TraceIdGenerator idGenerator;

	@Autowired
	private JobRuntimeListenerContainer jobRuntimeListenerContainer;

	@Autowired(required = false)
	private JavaMailService mailService;

	@Override
	protected long getTraceId(JobKey jobKey) {
		long traceId = idGenerator.generateTraceId(jobKey);
		setCustomizedLog(JobLoggerFactory.getLogger(log, traceId, jobKey, logManager));
		return traceId;
	}

	@Override
	public void execute(Job job, Object attachment, int retries) {
		runJob(job, attachment, retries);
	}

	@Override
	protected boolean isScheduling(JobKey jobKey, Job job) {
		return true;
	}

	@Override
	protected void notifyDependants(JobKey jobKey, Job job, Object result) {
		try {
			if (jobManager.hasRelations(jobKey, DependencyType.SERIAL)) {
				serialDependencyScheduler.notifyDependants(jobKey, result);
			}
		} catch (Exception e) {
			throw new JobException(e.getMessage(), e);
		}
	}

	@Override
	protected Object retry(JobKey jobKey, Job job, Object attachment, Throwable reason, int retries, Logger log) throws Throwable {
		return retryPolicy.retryIfNecessary(jobKey, job, attachment, reason, retries, log);
	}

	@Override
	protected void afterRun(long traceId, JobKey jobKey, Job job, Object attachment, Date startDate, RunningState runningState,
			Object result, Throwable reason, int retries) {
		super.afterRun(traceId, jobKey, job, attachment, startDate, runningState, result, reason, retries);
		stopWatch.onJobEnd(traceId, jobKey, startDate, runningState, retries);
		jobRuntimeListenerContainer.afterRun(traceId, jobKey, job, attachment, startDate, runningState, result, reason, retries);
	}

	@Override
	protected void sendMail(String mailAddress, long traceId, JobKey jobKey, Object attachment, Date startDate, RunningState runningState,
			Throwable reason) {
		if (mailService != null) {
			mailService.sendMail(mailAddress, traceId, jobKey, attachment, startDate, runningState, reason);
		}
	}
}