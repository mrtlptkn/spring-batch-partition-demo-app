package com.mrtlptkn.springbatchpartitiondemoapp.sample01;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.stereotype.Service;

@Service
public class OrderPartitionJobService {

    private final JobLauncher jobLauncher;
    private final Job partitionJob;

    public OrderPartitionJobService(JobLauncher jobLauncher, Job partitionJob) {
        this.jobLauncher = jobLauncher;
        this.partitionJob = partitionJob;
    }

    public JobExecution launch(String runDate,String failPartitionName) throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {

        var params =  new JobParametersBuilder()
                .addString("runDate",runDate)
                .addString("failPartitionName",failPartitionName)
                .toJobParameters();

        JobExecution ex =  jobLauncher.run(partitionJob,params);
        return ex;

    }

}
