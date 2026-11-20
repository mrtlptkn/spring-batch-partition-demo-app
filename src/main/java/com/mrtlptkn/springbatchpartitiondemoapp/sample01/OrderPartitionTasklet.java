package com.mrtlptkn.springbatchpartitiondemoapp.sample01;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@StepScope
public class OrderPartitionTasklet implements Tasklet {


    @Value("#{stepExecutionContext['minId']}")
    private Long minId;

    @Value("#{stepExecutionContext['maxId']}")
    private Long maxId;

    @Value("#{stepExecutionContext['partitionName']}")
    private String partitionName;

    @Value("#{jobParameters['runDate']}")
    private String runDate;

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderPartitionTasklet.class);
    private final JdbcTemplate jdbcTemplate;

    public OrderPartitionTasklet(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

        String thread = Thread.currentThread().getName();
        LOGGER.info("[{}] {} başladı, id aralığı {}-{}", thread, partitionName, minId, maxId);

        // Aynı gün partionlardan birisi başarısız olursa bu durumda bu tabloya aynı hesaplamalar tekrar atılabilir bunu sağlamak için chunk oriented yapı kullanmadığımız için tasklet bu chunkları yönetimini chunk gibi yapamdığından dolayı veriyi silip tekrar insert etmezsek sıkıntı yaşarız.
        jdbcTemplate.update("DELETE FROM customer_totals WHERE run_date = ? AND partition_name = ?", runDate, partitionName);
        contribution.incrementProcessSkipCount(); // Sayaçlarda manuel 1 adet process işlemi yapıldı yansısın diye yaptık.
        // WHERE order_date -> raporu gönderdiğimiz tarihe göre o günkü müşteri harcamalarının toplamını hesaplayacağız.
        // günlük bir rapor çalıştırığımızı düşünürsek
        String sqlQuery = "INSERT INTO customer_totals(run_date,customer,partition_name,total_amount) SELECT CAST(? AS DATE), customer, CAST(? AS VARCHAR(50)), SUM(amount) FROM orders WHERE order_id BETWEEN ? AND ? GROUP BY customer";

        int writeCount =  jdbcTemplate.update(sqlQuery,runDate,partitionName,minId,maxId);
        contribution.incrementWriteCount(writeCount); // Bu partition aralığında toplamda kaç kayıt customer_totals tablosuna yazıldı.
        LOGGER.info("[{}] {} tamamlandı, {} müşteri satırı yazıldı", thread, partitionName, writeCount);
        return RepeatStatus.FINISHED;
    }
}
