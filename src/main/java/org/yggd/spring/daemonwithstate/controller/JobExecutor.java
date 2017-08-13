package org.yggd.spring.daemonwithstate.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.converter.JobParametersConverter;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.support.PropertiesConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.yggd.spring.daemonwithstate.repository.BatchJobRequest;
import org.yggd.spring.daemonwithstate.repository.JobRequestRepository;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class JobExecutor {

    private static final Logger logger = LoggerFactory.getLogger(JobExecutor.class);

    private final Map<String, Job> localJobRegistry;

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private JobRequestRepository jobRequestRepository;

    @Autowired
    private JobParametersConverter jobParametersConverter;

    @Autowired
    private PlatformTransactionManager transactionManager;

    public JobExecutor(Collection<Job> jobs) {
        localJobRegistry = jobs.stream().collect(Collectors.toMap(Job::getName, j -> j));
    }

    @Async
    public void executeJob(BatchJobRequest batchJobRequest) {
        final Job job = localJobRegistry.get(batchJobRequest.getJobName());
        if (job == null) {
            logger.warn("skip job execution: {} is not exist.", batchJobRequest.getJobName());
            return;
        }
        final JobParameters jobParameters = jobParametersConverter.getJobParameters(
                PropertiesConverter.stringToProperties(batchJobRequest.getJobParameter()));
        try {
            batchJobRequest.setJobExecutionId(jobLauncher.run(job, jobParameters).getJobId());
            endJobExecution(batchJobRequest);
        } catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException
                | JobParametersInvalidException e) {
            logger.error("Job execution failed.", e);
        }
    }

    void endJobExecution(BatchJobRequest batchJobRequest) {
        new TransactionTemplate(transactionManager).<Void>execute( t -> {
            batchJobRequest.setPollingStatus("EXECUTED");
            int count = jobRequestRepository.updateStatus(batchJobRequest, "POLLED");
            if (count != 1) {
                throw new IllegalStateException(String.format("Illegal update count. jobSequenceId[%d] update count[%d]",
                        batchJobRequest.getJobSequenceId(), count));
            }
            return null;
        });
    }
}
