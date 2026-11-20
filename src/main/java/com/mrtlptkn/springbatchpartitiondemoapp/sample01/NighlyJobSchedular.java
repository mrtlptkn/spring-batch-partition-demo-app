package com.mrtlptkn.springbatchpartitiondemoapp.sample01;

import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

// mvn spring-boot:run -Dspring-boot.run.arguments="--batch.schedule.enabled=true"

@Component
@ConditionalOnProperty(name = "batch.schedular.enabled", havingValue = "true")
public class NighlyJobSchedular {

    private final OrderPartitionJobService  jobService;
    private final static ZoneId ISTANBUl = ZoneId.of("Europe/Istanbul");

    public NighlyJobSchedular(OrderPartitionJobService jobService) {
        this.jobService = jobService;
    }

    @Scheduled(cron = "${batch.schedular.cron}", zone = "Europe/Istanbul")
    public void runNightly() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        System.out.println("NighlyJobSchedular.runNightly() çalıştı");

        //LocalDate now = LocalDate.now(ISTANBUl);

        // Not: Kayıt tarihi olduğu için böyle yaptık. Bugün tarihli kayıt yok.
        String runDate = LocalDate.parse("2026-01-03").toString();
        String failPartitionName = "none";

        String result =  jobService.launch(runDate,failPartitionName).getStatus().toString();
        System.out.println("NighlyJobSchedular.runNightly() result: " + result);
    }

}
