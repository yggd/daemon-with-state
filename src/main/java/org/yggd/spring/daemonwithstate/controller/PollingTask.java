package org.yggd.spring.daemonwithstate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.yggd.spring.daemonwithstate.receiver.ReceiveMessage;
import org.yggd.spring.daemonwithstate.repository.BatchJobRequest;
import org.yggd.spring.daemonwithstate.repository.JobRequestRepository;

import java.util.List;

@Component
public class PollingTask {

    @Autowired
    private JobRequestRepository jobRequestRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private JobExecutor jobExecutor;

    @Value("#{${app.task-executor.size} - 1}")
    private int rowLimit;

    public void execute(ReceiveMessage receiveMessage) {
        doPoll(receiveMessage.getJobName(), receiveMessage.getJobParameters()).forEach( b -> {
            jobExecutor.executeJob(b); // execute async.
        });
    }

    public List<BatchJobRequest> doPoll(final String jobName, final String jobParameters) {
        return new TransactionTemplate(transactionManager).<List<BatchJobRequest>>execute( t -> {
            final List<BatchJobRequest> jobRequests = jobRequestRepository.findForUpdate(jobName, rowLimit);
            jobRequests.forEach( j -> {
                if (jobParameters != null && !"".equals(jobParameters)) {
                    j.setJobParameter(jobParameters);
                }
                j.setPollingStatus("POLLED");
                int count = jobRequestRepository.updateStatus(j, "INIT");
                if (count != 1) {
                    throw new IllegalStateException(String.format("Illegal update count. jobSequenceId[%d] update count[%d]",
                            j.getJobSequenceId(), count));
                }
            });
            return jobRequests;
        });
    }
}
