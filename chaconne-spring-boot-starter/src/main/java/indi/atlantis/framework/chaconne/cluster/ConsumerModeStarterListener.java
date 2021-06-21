package indi.atlantis.framework.chaconne.cluster;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.github.paganini2008.devtools.ArrayUtils;

import indi.atlantis.framework.chaconne.DependencyType;
import indi.atlantis.framework.chaconne.ForkJoinJobListener;
import indi.atlantis.framework.chaconne.JobKey;
import indi.atlantis.framework.chaconne.JobListenerContainer;
import indi.atlantis.framework.chaconne.JobManager;
import indi.atlantis.framework.chaconne.TriggerType;
import indi.atlantis.framework.chaconne.model.JobKeyQuery;
import indi.atlantis.framework.chaconne.model.Result;
import indi.atlantis.framework.tridenter.ApplicationInfo;
import indi.atlantis.framework.tridenter.InstanceId;
import indi.atlantis.framework.tridenter.election.ApplicationClusterRefreshedEvent;
import indi.atlantis.framework.tridenter.utils.ApplicationContextUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * ConsumerModeStarterListener
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
public class ConsumerModeStarterListener implements ApplicationListener<ApplicationClusterRefreshedEvent> {

	@Value("${spring.application.cluster.name}")
	private String clusterName;

	@Autowired
	private JobManager jobManager;

	@Autowired
	private InstanceId instanceId;

	@Autowired
	private ConsumerModeRestTemplate restTemplate;

	@Autowired
	private JobListenerContainer jobListenerContainer;

	@Override
	public void onApplicationEvent(ApplicationClusterRefreshedEvent event) {
		registerCluster();

		handleParallelDependencies();
	}

	private void registerCluster() {
		final ApplicationInfo applicationInfo = instanceId.getApplicationInfo();
		ResponseEntity<Result<Boolean>> responseEntity = restTemplate.perform(null, "/job/admin/registerCluster", HttpMethod.POST,
				applicationInfo, new ParameterizedTypeReference<Result<Boolean>>() {
				});
		if (responseEntity.getStatusCode() == HttpStatus.OK && responseEntity.getBody().getData()) {
			log.info("'{}' register to job producer ok.", applicationInfo);
		}
	}

	private void handleParallelDependencies() {
		JobKeyQuery jobQuery = new JobKeyQuery();
		jobQuery.setClusterName(clusterName);
		jobQuery.setTriggerType(TriggerType.DEPENDENT);
		try {
			JobKey[] jobKeys = jobManager.getJobKeys(jobQuery);
			if (ArrayUtils.isNotEmpty(jobKeys)) {
				JobKey[] dependentKeys;
				for (JobKey jobKey : jobKeys) {
					// add listener to watch parallel dependency job done
					dependentKeys = jobManager.getDependentKeys(jobKey, DependencyType.PARALLEL);
					if (ArrayUtils.isNotEmpty(dependentKeys)) {
						for (JobKey dependency : dependentKeys) {
							jobListenerContainer.addListener(dependency,
									ApplicationContextUtils.instantiateClass(ForkJoinJobListener.class));
						}
					}
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

}
