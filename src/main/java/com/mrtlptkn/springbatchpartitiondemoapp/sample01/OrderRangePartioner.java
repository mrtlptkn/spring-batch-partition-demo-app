package com.mrtlptkn.springbatchpartitiondemoapp.sample01;

// Burada order nesnesini parçalara bölmenin algoritması yazılacak
// Parçalar üzerinden ise GridSize thread sayısını ayarlayacağız.


import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

public class OrderRangePartioner implements Partitioner {

    // Önce min Max Id değerlerini bularak ne kadarlık bir veri kümesi var bulalım
    private final JdbcTemplate jdbcTemplate;

    public OrderRangePartioner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {

        Map<String,ExecutionContext> partitions = new HashMap<>();

        // 1
        Long min = jdbcTemplate.queryForObject("SELECT MIN(order_id) FROM orders", Long.class);
        Long max = jdbcTemplate.queryForObject("SELECT MAX(order_id) FROM orders", Long.class);
        // 50.000

        if(min == null || max == null){
            return partitions;
        }

        // gridSize -> Bu iş için kullanılacak olan maksimum thread sayısı
        // total: 10 -> gridSize: 4 -> pageSize: aşağı yuvarlama 2 yukarı yuvarladığımız da 3
        // pageSize aralık
        // 1,2,3  partition1  // 1,2
        // 4,5,6 partions2    // 3,4
        // 7,8,9 partition3   // 5,6
        // 10 partition4      // 7,8 //9,10 partition5 -> ThreadPooldan fazladan 1 thread istemek zorunda kalırız.
        long total = max - min + 1; // 500.000
        long partitionSize = (long)Math.ceil((double)total/gridSize); // 500.000 / 4 = 125.000

        int index = 0;
        for (long start=min;start<=max;start+=partitionSize, index++){

            String name = "partition" + index;
            ExecutionContext context = new ExecutionContext();
            context.putLong("minId", start); // Id nerede başlar
            context.putLong("maxId", Math.min(start + partitionSize - 1, max)); // Id nerede biter
            context.putString("partitionName", name);

            // 1-125.000 partition0 -> Thread1
            // 125.000 250.000 partition1 -> Thread2
            // 250.000 375.000 partition2 -> Thread3
            // 375.000 500.000 partition3 -> Thread4

            partitions.put(name, context);
        }

        return partitions;
    }
}
