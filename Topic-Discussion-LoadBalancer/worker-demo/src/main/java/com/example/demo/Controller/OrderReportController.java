package com.example.demo.Controller;

import com.example.demo.Service.ReportService;
import com.example.demo.dto.Pong;
import com.example.demo.dto.Work;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class OrderReportController {
    private final ReportService reportService;

    public OrderReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/ping")
    public ResponseEntity<Map<String, Pong>> ping() {
        Pong pong = new Pong();
        return ResponseEntity.ok(Map.of("data", pong));
    }

    @GetMapping("/work")
    public ResponseEntity<?> getReport() {
        Pong pong = new Pong();
        Work work = new Work( pong ,reportService.reportOrders());
        return ResponseEntity.ok(work) ;
    }
}

