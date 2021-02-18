package indi.atlantis.framework.jobhub.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import com.github.paganini2008.devtools.ArrayUtils;

import indi.atlantis.framework.jobhub.BeanNames;
import indi.atlantis.framework.jobhub.DependencyType;
import indi.atlantis.framework.jobhub.Job;
import indi.atlantis.framework.jobhub.JobBeanInitializer;
import indi.atlantis.framework.jobhub.JobBeanLoader;
import indi.atlantis.framework.jobhub.JobFutureHolder;
import indi.atlantis.framework.jobhub.JobKey;
import indi.atlantis.framework.jobhub.JobManager;
import indi.atlantis.framework.jobhub.SerialDependencyScheduler;
import indi.atlantis.framework.jobhub.TriggerType;
import indi.atlantis.framework.jobhub.model.JobKeyQuery;

/**
 * 
 * ConsumerModeJobBeanInitializer
 * 
 * @author Jimmy Hoff
 *
 * @since 1.0
 */
public class ConsumerModeJobBeanInitializer implements JobBeanInitializer {

	@Value("${spring.application.cluster.name}")
	private String clusterName;

	@Autowired
	private JobManager jobManager;

	@Qualifier(BeanNames.INTERNAL_JOB_BEAN_LOADER)
	@Autowired
	private JobBeanLoader jobBeanLoader;

	@Qualifier(BeanNames.EXTERNAL_JOB_BEAN_LOADER)
	@Autowired(required = false)
	private JobBeanLoader externalJobBeanLoader;

	@Autowired
	private SerialDependencyScheduler serialDependencyScheduler;

	@Autowired
	private JobFutureHolder jobFutureHolder;

	public void initializeJobBeans() throws Exception {
		handleSerialJobDependencies();
	}

	private void handleSerialJobDependencies() throws Exception {
		JobKeyQuery jobQuery = new JobKeyQuery();
		jobQuery.setClusterName(clusterName);
		jobQuery.setTriggerType(TriggerType.DEPENDENT);
		JobKey[] jobKeys = jobManager.getJobKeys(jobQuery);
		if (ArrayUtils.isNotEmpty(jobKeys)) {
			Job job;
			JobKey[] dependentKeys;
			for (JobKey jobKey : jobKeys) {
				job = jobBeanLoader.loadJobBean(jobKey);
				if (job == null && externalJobBeanLoader != null) {
					job = externalJobBeanLoader.loadJobBean(jobKey);
				}
				if (job == null) {
					continue;
				}
				// update or schedule serial dependency job
				dependentKeys = jobManager.getDependentKeys(jobKey, DependencyType.SERIAL);
				if (ArrayUtils.isNotEmpty(dependentKeys)) {
					if (serialDependencyScheduler.hasScheduled(jobKey)) {
						serialDependencyScheduler.updateDependency(job, dependentKeys);
					} else {
						jobFutureHolder.add(jobKey, serialDependencyScheduler.scheduleDependency(job, dependentKeys));
					}
				}

			}
		}
	}

}