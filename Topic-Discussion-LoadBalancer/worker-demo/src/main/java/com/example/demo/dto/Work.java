package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class Work {
    @JsonProperty("meta")
    private Pong pong;

    private List<Map<String, Object>> data;
}
