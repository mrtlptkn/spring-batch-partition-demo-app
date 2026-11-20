package com.mrtlptkn.springbatchpartitiondemoapp.sample01;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/partition-job")
public class PartitionJobController {

    private final OrderPartitionJobService  jobService;

    public PartitionJobController(OrderPartitionJobService jobService) {
        this.jobService = jobService;
    }

    @PostMapping
    public String launchPartitionJob(@RequestParam("runDate") String runDate,@RequestParam("failPartitionName") String failPartitionName) throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        return jobService.launch(runDate,failPartitionName).getStatus().toString();
    }
}
