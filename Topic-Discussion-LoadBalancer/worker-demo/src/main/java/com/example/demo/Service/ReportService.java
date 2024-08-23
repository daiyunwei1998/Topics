package com.example.demo.Service;

import com.example.demo.Repository.OrderDAO;
import com.example.demo.model.Order;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportService {
    private final OrderDAO orderDAO;
    private final ObjectMapper objectMapper;

    private static final String REDIS_LIST_KEY = "orderProcessTasks";

    public ReportService(OrderDAO orderDAO, ObjectMapper objectMapper) {
        this.orderDAO = orderDAO;
        this.objectMapper = objectMapper;
    }

    public List<Map<String, Object>> reportOrders() {
        List<Order> orderList = orderDAO.getOrdersTotal();
        Map<Long, BigDecimal> aggregatedTotals = orderList.stream()
                .collect(Collectors.groupingBy(
                        Order::getUserId,  // Group by user_id
                        Collectors.reducing(
                                BigDecimal.ZERO,    // Identity value for reduction
                                Order::getTotal,    // Extract BigDecimal total from Order
                                BigDecimal::add     // Accumulate totals
                        )
                ));

        List<Map<String, Object>> result = aggregatedTotals.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("user_id", entry.getKey());
                    map.put("total_payment", entry.getValue());
                    return map;
                })
                .collect(Collectors.toList());

        return result;
    }

}
