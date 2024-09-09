package org.example.worktest.controller;


import org.example.worktest.entity.Order;
import org.example.worktest.service.OrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/test")
public class TestController {

    private final OrderService orderService;

    public TestController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public List<Order> listOrder() {
        return orderService.list();
    }
}
