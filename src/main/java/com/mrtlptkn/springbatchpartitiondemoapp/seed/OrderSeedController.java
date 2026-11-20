package com.mrtlptkn.springbatchpartitiondemoapp.seed;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/seed")
public class OrderSeedController {

    private final OrderSeed orderSeed;

    public OrderSeedController(OrderSeed orderSeed) {
        this.orderSeed = orderSeed;
    }

    @PostMapping
    public String seedOrders(@RequestParam("value") int value) {
        int count = orderSeed.seed(value);
        return "Seeded " + count + " orders.";
    }

}
