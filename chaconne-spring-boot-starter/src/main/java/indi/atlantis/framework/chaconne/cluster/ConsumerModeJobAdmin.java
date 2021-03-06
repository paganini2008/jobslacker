/**
* Copyright 2018-2021 Fred Feng (paganini.fy@gmail.com)

* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package indi.atlantis.framework.chaconne.cluster;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import indi.atlantis.framework.chaconne.ChaconneBeanNames;
import indi.atlantis.framework.chaconne.Job;
import indi.atlantis.framework.chaconne.JobAdmin;
import indi.atlantis.framework.chaconne.JobBeanLoader;
import indi.atlantis.framework.chaconne.JobExecutor;
import indi.atlantis.framework.chaconne.JobKey;
import indi.atlantis.framework.chaconne.JobLifeCycle;
import indi.atlantis.framework.chaconne.JobManager;
import indi.atlantis.framework.chaconne.JobState;
import indi.atlantis.framework.chaconne.LifeCycleListenerContainer;
import indi.atlantis.framework.chaconne.model.JobLifeCycleParameter;
import indi.atlantis.framework.tridenter.multicast.ApplicationMulticastGroup;

/**
 * 
 * ConsumerModeJobAdmin
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
public class ConsumerModeJobAdmin implements JobAdmin {

	@Value("${spring.application.name}")
	private String applicationName;

	@Qualifier(ChaconneBeanNames.MAIN_JOB_EXECUTOR)
	@Autowired
	private JobExecutor jobExecutor;

	@Qualifier(ChaconneBeanNames.INTERNAL_JOB_BEAN_LOADER)
	@Autowired
	private JobBeanLoader jobBeanLoader;

	@Qualifier(ChaconneBeanNames.EXTERNAL_JOB_BEAN_LOADER)
	@Autowired(required = false)
	private JobBeanLoader externalJobBeanLoader;

	@Autowired
	private JobManager jobManager;

	@Autowired
	private ApplicationMulticastGroup applicationMulticastGroup;

	@Override
	public JobState triggerJob(JobKey jobKey, Object attachment) throws Exception {
		Job job = loadJobBean(jobKey);
		jobExecutor.execute(job, attachment, 0);
		return jobManager.getJobRuntimeDetail(jobKey).getJobState();
	}

	@Override
	public void publicLifeCycleEvent(JobKey jobKey, JobLifeCycle lifeCycle) {
		applicationMulticastGroup.multicast(applicationName, LifeCycleListenerContainer.class.getName(),
				new JobLifeCycleParameter(jobKey, lifeCycle));
	}

	private Job loadJobBean(JobKey jobKey) throws Exception {
		Job job = jobBeanLoader.loadJobBean(jobKey);
		if (job == null && externalJobBeanLoader != null) {
			job = externalJobBeanLoader.loadJobBean(jobKey);
		}
		return job;
	}

	@Override
	public JobState scheduleJob(JobKey jobKey) {
		throw new UnsupportedOperationException("Calling scheduleJob for ConsumerMode");
	}

	@Override
	public JobState unscheduleJob(JobKey jobKey) {
		throw new UnsupportedOperationException("Calling unscheduleJob for ConsumerMode");
	}

}
