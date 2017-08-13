package org.yggd.spring.daemonwithstate.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;

// Can't use @Component with @Scope("step") in java-config.
public class SampleTasklet implements Tasklet {

    private static final Logger logger = LoggerFactory.getLogger(SampleTasklet.class);

    @Value("#{jobParameters[param1] ?: 'default'}")
    private String param1;

    @Value("#{jobParameters[param2] ?: 'default'}")
    private String param2;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        logger.info("tasklet called. param1={},param2={}", param1, param2);
        return RepeatStatus.FINISHED;
    }

}
