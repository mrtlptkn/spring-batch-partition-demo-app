package com.mrtlptkn.springbatchpartitiondemoapp.sample01;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.partition.PartitionHandler;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class PartionerJobConfig {

    @Value("${batch.partition.grid-size:4}")
    private int gridSize; // Thread havuzunun boyutu

    private final OrderPartitionTasklet orderPartitionTasklet;

    public PartionerJobConfig(OrderPartitionTasklet orderPartitionTasklet) {
        this.orderPartitionTasklet = orderPartitionTasklet;
    }

    @Bean
    public ThreadPoolTaskExecutor threadPoolTaskExecutor(){
        // Belirlediğim sizedaki thread'e böl işlemi (GridSize)
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(gridSize);
        taskExecutor.setMaxPoolSize(gridSize); // Thread sınırını aşma, eğer thread yetersiz ise diğer threadlerin bitmesini bekle, kuyruklama yap.
        taskExecutor.setThreadNamePrefix("partition-thread-");
        taskExecutor.initialize();
        return taskExecutor;
    }

    @Bean
    public Partitioner orderRangePartitioner(JdbcTemplate jdbcTemplate) {
        return new OrderRangePartioner(jdbcTemplate);
    }

    @Bean
    public Step workerStep(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager){
        return  new StepBuilder("workerStep",jobRepository)
                .tasklet(orderPartitionTasklet,platformTransactionManager)
                .build();
    }

    @Bean
    public PartitionHandler orderRangePartitionHandler(ThreadPoolTaskExecutor threadPoolTaskExecutor,Step workerStep){
        TaskExecutorPartitionHandler handler = new TaskExecutorPartitionHandler();
        handler.setStep(workerStep);
        handler.setTaskExecutor(threadPoolTaskExecutor);
        handler.setGridSize(gridSize);
        return handler;
    }

    // MasterStepi workerSteplere orderRangePartitioner algoritması ile orderRangePartitionHandler split edicideki ayarlar ile bölümle. Paralel olarak Threadlere böl.

    @Bean
    public Step masterStep(JobRepository jobRepository,Partitioner orderRangePartitioner,PartitionHandler orderRangePartitionHandler){
        return  new StepBuilder("masterStep",jobRepository)
                .partitioner("workerStep",orderRangePartitioner)
                .partitionHandler(orderRangePartitionHandler)
                .build();
    }

    // Step Execution da aşağıdaki hali ile görürüz.
    // masterStep: 100 commit -> job start ederken bu stepten başlar, aşağıdaki steplere bölümlenir.
    // workerStep:partition0 -> 25 commit (COMPLETED) STARTDATE:20.11.2026 14:02
    // workerStep:partition1 -> 25 commit (FAILED) STARTDATE:20.11.2026 14:02
    // workerStep:partition2 -> 25 commit (COMPLETED) STARTDATE:20.11.2026 14:02
    // workerStep:partition3 -> 25 commit (COMPLETED) STARTDATE:20.11.2026 14:02

    // Paralel olduklarından dolayı aynı anda başlarlar.
    // JOB: FAILED -> restart ile ikinci çalıştırmada sadece partition1 çalışacak.

    // Bu aşamda split manuel olarak burada yönetmiyoruz bunu bizim yerimize partitionHandler bean üstlenir.
    @Bean
    public Job partitionJob(JobRepository jobRepository,Step masterStep){
        return new JobBuilder("partitionJob",jobRepository)
                .start(masterStep)
                .build();
    }

}
