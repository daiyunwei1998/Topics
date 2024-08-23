package com.example.demo.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.RedisTemplate;

@Service
public class RedisService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    // Save metadata to Redis with worker name as the main key, timestamp as the field name, and metadata as the value
    public void saveValue(String workerName, String timestamp, String metadata) {
        redisTemplate.opsForHash().put(workerName, timestamp, metadata);
        publishEvent(workerName, timestamp);
    }

    // Retrieve metadata from Redis using worker name and timestamp
    public String getValue(String workerName, String timestamp) {
        return (String) redisTemplate.opsForHash().get(workerName, timestamp);
    }

    // Delete metadata from Redis using worker name and timestamp
    public void deleteValue(String workerName, String timestamp) {
        redisTemplate.opsForHash().delete(workerName, timestamp);
    }

    // Publish an event to a Redis channel
    public void publishEvent(String workerName, String timestamp) {
        String channel = "data_stream";  // The channel to which you'll publish events
        String message = workerName + ":" + timestamp;  // Message format
        redisTemplate.convertAndSend(channel, message);  // Publish the message
    }
}