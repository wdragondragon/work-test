package org.example.worktest.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import org.example.worktest.entity.Order;
import org.example.worktest.mapper.OrderMapper;
import org.example.worktest.service.OrderService;
import org.springframework.stereotype.Service;

@DS("datax_test")
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {
}
