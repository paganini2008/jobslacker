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
package indi.atlantis.framework.chaconne;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.github.paganini2008.springdessert.reditools.messager.RedisMessageHandler;

import indi.atlantis.framework.chaconne.model.JobParameter;
import indi.atlantis.framework.tridenter.Constants;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * SerialDependencyHandler
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Slf4j
public class SerialDependencyHandler implements RedisMessageHandler {

	static final String BEAN_NAME = SerialDependencyHandler.class.getSimpleName();

	@Value("${spring.application.cluster.name}")
	private String clusterName;

	@Autowired
	private SerialDependencyScheduler serialDependencyScheduler;

	@Override
	public String getChannel() {
		return Constants.APPLICATION_CLUSTER_NAMESPACE + clusterName + ":scheduler:job:dependency:*";
	}

	@Override
	public void onMessage(String channel, Object message) throws Exception {
		final JobParameter jobParam = (JobParameter) message;
		if (log.isTraceEnabled()) {
			log.trace("Trigger all serial dependencies by Job: " + jobParam.getJobKey());
		}
		serialDependencyScheduler.triggerDependency(jobParam.getJobKey(), jobParam.getAttachment());
	}

}
