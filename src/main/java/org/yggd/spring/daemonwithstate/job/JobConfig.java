package org.yggd.spring.daemonwithstate.job;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
public class JobConfig {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Bean
    public Job sampleJob(Step sampleStep) {
        return jobBuilderFactory.get("sampleJob")
                .start(sampleStep)
                .build();
    }

    @Bean
    public Step sampleStep(SampleTasklet sampleTasklet) {
        return stepBuilderFactory.get("sampleJobStep")
                .transactionManager(transactionManager)
                .tasklet(sampleTasklet)
                .build();
    }

    @Bean
    @StepScope
    public SampleTasklet sampleTasklet() {
        return new SampleTasklet();
    }
}
