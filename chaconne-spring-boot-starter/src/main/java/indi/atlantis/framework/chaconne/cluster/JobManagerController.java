package indi.atlantis.framework.chaconne.cluster;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import indi.atlantis.framework.chaconne.JobKey;
import indi.atlantis.framework.chaconne.JobManager;
import indi.atlantis.framework.chaconne.JobState;
import indi.atlantis.framework.chaconne.LogManager;
import indi.atlantis.framework.chaconne.StopWatch;
import indi.atlantis.framework.chaconne.TraceIdGenerator;
import indi.atlantis.framework.chaconne.model.JobDependencyParameter;
import indi.atlantis.framework.chaconne.model.JobDetail;
import indi.atlantis.framework.chaconne.model.JobKeyQuery;
import indi.atlantis.framework.chaconne.model.JobLog;
import indi.atlantis.framework.chaconne.model.JobLogParameter;
import indi.atlantis.framework.chaconne.model.JobPersistParameter;
import indi.atlantis.framework.chaconne.model.JobResult;
import indi.atlantis.framework.chaconne.model.JobRuntimeDetail;
import indi.atlantis.framework.chaconne.model.JobRuntimeParameter;
import indi.atlantis.framework.chaconne.model.JobStackTrace;
import indi.atlantis.framework.chaconne.model.JobStateParameter;
import indi.atlantis.framework.chaconne.model.JobTrace;
import indi.atlantis.framework.chaconne.model.JobTracePageQuery;
import indi.atlantis.framework.chaconne.model.JobTraceQuery;
import indi.atlantis.framework.chaconne.model.JobTriggerDetail;
import indi.atlantis.framework.chaconne.model.PageQuery;

/**
 * 
 * JobManagerController
 * 
 * @author Fred Feng
 *
 * @since 1.0
 */
@RestController
@RequestMapping("/job/manager")
public class JobManagerController {

	@Autowired
	private JobManager jobManager;

	@Autowired
	private LogManager logManager;

	@Autowired
	private StopWatch stopWatch;

	@Autowired
	private TraceIdGenerator traceIdGenerator;

	@GetMapping("/selectClusterNames")
	public ResponseEntity<JobResult<String[]>> selectClusterNames() throws Exception {
		String[] clusterNames = jobManager.selectClusterNames();
		return ResponseEntity.ok(JobResult.success(clusterNames));
	}

	@PostMapping("/persistJob")
	public ResponseEntity<JobResult<Integer>> persistJob(@RequestBody JobPersistParameter parameter) throws Exception {
		int jobId = jobManager.persistJob(parameter);
		return ResponseEntity.ok(JobResult.success(jobId));
	}

	@PostMapping("/finishJob")
	public ResponseEntity<JobResult<JobState>> finishJob(@RequestBody JobKey jobKey) throws Exception {
		JobState jobState = jobManager.finishJob(jobKey);
		return ResponseEntity.ok(JobResult.success(jobState));
	}

	@PostMapping("/hasJob")
	public ResponseEntity<JobResult<Boolean>> hasJob(@RequestBody JobKey jobKey) throws Exception {
		boolean has = jobManager.hasJob(jobKey);
		return ResponseEntity.ok(JobResult.success(has));
	}

	@PostMapping("/hasJobState")
	public ResponseEntity<JobResult<Boolean>> hasJobState(@RequestBody JobStateParameter parameter) throws Exception {
		boolean has = jobManager.hasJobState(parameter.getJobKey(), parameter.getJobState());
		return ResponseEntity.ok(JobResult.success(has));
	}

	@PostMapping("/getJobId")
	public ResponseEntity<JobResult<Integer>> getJobId(@RequestBody JobKey jobKey) throws Exception {
		int jobId = jobManager.getJobId(jobKey);
		return ResponseEntity.ok(JobResult.success(jobId));
	}

	@PostMapping("/getJobDetail")
	public ResponseEntity<JobResult<JobDetail>> getJobDetail(@RequestBody JobKey jobKey) throws Exception {
		JobDetail jobDetail = jobManager.getJobDetail(jobKey, true);
		return ResponseEntity.ok(JobResult.success(jobDetail));
	}

	@PostMapping("/getJobTriggerDetail")
	public ResponseEntity<JobResult<JobTriggerDetail>> getJobTriggerDetail(@RequestBody JobKey jobKey) throws Exception {
		JobTriggerDetail jobTriggerDetail = jobManager.getJobTriggerDetail(jobKey);
		return ResponseEntity.ok(JobResult.success(jobTriggerDetail));
	}

	@PostMapping("/getJobRuntimeDetail")
	public ResponseEntity<JobResult<JobRuntimeDetail>> getJobRuntime(@RequestBody JobKey jobKey) throws Exception {
		JobRuntimeDetail jobRuntime = jobManager.getJobRuntimeDetail(jobKey);
		return ResponseEntity.ok(JobResult.success(jobRuntime));
	}

	@PostMapping("/hasRelations")
	public ResponseEntity<JobResult<Boolean>> hasRelations(@RequestBody JobDependencyParameter parameter) throws Exception {
		boolean result = jobManager.hasRelations(parameter.getJobKey(), parameter.getDependencyType());
		return ResponseEntity.ok(JobResult.success(result));
	}

	@PostMapping("/getRelations")
	public ResponseEntity<JobResult<JobKey[]>> getRelations(@RequestBody JobDependencyParameter parameter) throws Exception {
		JobKey[] jobKeys = jobManager.getRelations(parameter.getJobKey(), parameter.getDependencyType());
		return ResponseEntity.ok(JobResult.success(jobKeys));
	}

	@PostMapping("/getDependentKeys")
	public ResponseEntity<JobResult<JobKey[]>> getDependencies(@RequestBody JobDependencyParameter parameter) throws Exception {
		JobKey[] jobKeys = jobManager.getDependentKeys(parameter.getJobKey(), parameter.getDependencyType());
		return ResponseEntity.ok(JobResult.success(jobKeys));
	}

	@PostMapping("/setJobState")
	public ResponseEntity<JobResult<JobState>> setJobState(@RequestBody JobStateParameter parameter) throws Exception {
		JobState jobState = jobManager.setJobState(parameter.getJobKey(), parameter.getJobState());
		return ResponseEntity.ok(JobResult.success(jobState));
	}

	@PostMapping("/getJobKeys")
	public ResponseEntity<JobResult<JobKey[]>> getJobKeys(@RequestBody JobKeyQuery jobQuery) throws Exception {
		JobKey[] jobKeys = jobManager.getJobKeys(jobQuery);
		return ResponseEntity.ok(JobResult.success(jobKeys));
	}

	@PostMapping("/selectJobDetail")
	public ResponseEntity<JobResult<PageQuery<JobDetail>>> selectJobDetail(@RequestBody PageQuery<JobDetail> pageQuery) throws Exception {
		jobManager.selectJobDetail(pageQuery);
		return ResponseEntity.ok(JobResult.success(pageQuery));
	}

	@PostMapping("/selectJobTrace")
	public ResponseEntity<JobResult<PageQuery<JobTrace>>> selectJobTrace(@RequestBody JobTracePageQuery<JobTrace> pageQuery)
			throws Exception {
		jobManager.selectJobTrace(pageQuery);
		return ResponseEntity.ok(JobResult.success(pageQuery));
	}

	@PostMapping("/selectJobLog")
	public ResponseEntity<JobResult<JobLog[]>> selectJobLog(@RequestBody JobTraceQuery query) throws Exception {
		JobLog[] logs = jobManager.selectJobLog(query);
		return ResponseEntity.ok(JobResult.success(logs));
	}

	@PostMapping("/selectJobStackTrace")
	public ResponseEntity<JobResult<JobStackTrace[]>> selectJobStackTrace(@RequestBody JobTraceQuery query) throws Exception {
		JobStackTrace[] traces = jobManager.selectJobStackTrace(query);
		return ResponseEntity.ok(JobResult.success(traces));
	}

	@PostMapping("/onJobBegin")
	public ResponseEntity<JobResult<JobState>> onJobBegin(@RequestBody JobRuntimeParameter parameter) throws Exception {
		JobState jobState = stopWatch.onJobBegin(parameter.getTraceId(), parameter.getJobKey(), parameter.getStartTime());
		return ResponseEntity.ok(JobResult.success(jobState));
	}

	@PostMapping("/onJobEnd")
	public ResponseEntity<JobResult<JobState>> onJobEnd(@RequestBody JobRuntimeParameter parameter) throws Exception {
		JobState jobState = stopWatch.onJobEnd(parameter.getTraceId(), parameter.getJobKey(), parameter.getStartTime(), parameter.getRunningState(),
				parameter.getRetries());
		return ResponseEntity.ok(JobResult.success(jobState));
	}

	@PostMapping("/generateTraceId")
	public ResponseEntity<JobResult<Long>> generateTraceId(@RequestBody JobKey jobKey) {
		long traceId = traceIdGenerator.generateTraceId(jobKey);
		return ResponseEntity.ok(JobResult.success(traceId));
	}

	@PostMapping("/log")
	public ResponseEntity<JobResult<String>> log(@RequestBody JobLogParameter parameter) throws Exception {
		logManager.log(parameter.getTraceId(), parameter.getJobKey(), parameter.getLogLevel(), parameter.getMessagePattern(), parameter.getArgs(),
				parameter.getStackTraces());
		return ResponseEntity.ok(JobResult.success("ok"));
	}

}
