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
package indi.atlantis.framework.chaconne.model;

import indi.atlantis.framework.chaconne.DependencyType;
import indi.atlantis.framework.chaconne.JobKey;
import lombok.Getter;
import lombok.Setter;

/**
 * 
 * JobDependencyParameter
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@Getter
@Setter
public class JobDependencyParameter {

	private JobKey jobKey;
	private DependencyType dependencyType;

	public JobDependencyParameter() {
	}

	public JobDependencyParameter(JobKey jobKey, DependencyType dependencyType) {
		this.jobKey = jobKey;
		this.dependencyType = dependencyType;
	}

}
