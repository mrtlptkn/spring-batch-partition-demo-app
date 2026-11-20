package com.mrtlptkn.springbatchpartitiondemoapp.seed;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;

@Service
public class OrderSeed {

    private static  final List<String> CUSTOMERS = List.of("Ahmet", "Ayşe", "Mehmet", "Zeynep", "Can", "Elif", "Burak", "Deniz", "Selin", "Emre");

    private final JdbcTemplate jdbcTemplate;

    public OrderSeed(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // count: ne kadarlık bir veri size üreteceğiz
    public int seed(int count){

        jdbcTemplate.update("DELETE FROM customer_totals");
        jdbcTemplate.update("DELETE FROM orders");

        Random random = new Random(50);
        // 1 aylık sipariş dökümü üreteceğiz
        LocalDate startDate = LocalDate.of(2026,1,1);

        List<Object[]> rows = new java.util.ArrayList<>(count);

        for (long id = 1; id <= count; id++) {
            String customer = CUSTOMERS.get(random.nextInt(CUSTOMERS.size()));
            // 0.10 ile 5000.10 arasında rastgele tutar (kuruş cinsinden üretilir)
            BigDecimal amount =  BigDecimal.valueOf(100 + random.nextInt(50000), 2); // 497,66 302,56
            LocalDate orderDate = startDate.plusDays(random.nextInt(30));
            rows.add(new Object[]{id, customer, amount, java.sql.Date.valueOf(orderDate)});
        }

        jdbcTemplate.batchUpdate(
                "INSERT INTO orders (order_id, customer, amount, order_date) VALUES (?, ?, ?, ?)",
                rows);

        return count;
    }

}
