package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Pong {

    @JsonProperty("worker_name")
    private String workerName;

    @JsonProperty("ip_address")
    private String ipAddress;

    @JsonProperty("time_stamp")
    private String timeStamp;

    @JsonGetter("worker_name")
    public String getWorkerName() {
        return System.getenv("WORKER_NAME");
    }

    @JsonGetter("ip_address")
    public String getIpAddress() {
        return System.getenv("IP_ADDRESS");
    }

    @JsonGetter("time_stamp")
    public String getTimeStamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                .withZone(ZoneId.of("Asia/Taipei"));
        return formatter.format(Instant.now());
    }
}