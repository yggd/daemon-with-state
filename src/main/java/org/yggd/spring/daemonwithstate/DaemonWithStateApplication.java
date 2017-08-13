package org.yggd.spring.daemonwithstate;

import org.h2.server.web.WebServlet;
import org.springframework.batch.core.converter.JobParametersConverter;
import org.springframework.batch.core.jsr.JsrJobParametersConverter;
import org.springframework.batch.core.launch.support.CommandLineJobRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.yggd.spring.daemonwithstate.controller.BatchDaemon;
import org.yggd.spring.daemonwithstate.receiver.ReceiveMessage;

import javax.sql.DataSource;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

@SpringBootApplication
@EnableAsync
public class DaemonWithStateApplication implements CommandLineRunner {

    @Bean
    public ServletRegistrationBean h2Servlet() {
        final ServletRegistrationBean bean = new ServletRegistrationBean(new WebServlet());
        bean.addUrlMappings("/console/*");
        return bean;
    }

    @Bean
    public ThreadPoolTaskExecutor taskExecutor(
            @Value("${app.task-executor.size}") int size,
            @Value("${app.task-executor.capacity}")int capacity) {
        final ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(size);
        taskExecutor.setMaxPoolSize(size);
        taskExecutor.setQueueCapacity(capacity);
        taskExecutor.setWaitForTasksToCompleteOnShutdown(true);
        return taskExecutor;
    }

    @Bean
    public BlockingQueue<ReceiveMessage> queue(@Value("${app.task-executor.capacity}") int capacity) {
        return new ArrayBlockingQueue<>(capacity, true);
    }

    @Bean
    public JobParametersConverter jobParametersConverter(DataSource dataSource) {
        return new JsrJobParametersConverter(dataSource);
    }

    @Autowired
    private BatchDaemon batchDaemon;

    @Override
    public void run(String... strings) throws Exception {
        batchDaemon.start();
    }

    public static void main(String[] args) {
        SpringApplication.run(DaemonWithStateApplication.class, args).close();
    }
}
