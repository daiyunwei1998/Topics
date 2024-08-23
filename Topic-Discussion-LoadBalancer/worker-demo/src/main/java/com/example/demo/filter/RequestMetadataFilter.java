package com.example.demo.filter;

import com.example.demo.Repository.RequestMetadataRepository;
import com.example.demo.Service.DataWebSocketHandler;
import com.example.demo.Service.RedisService;
import com.example.demo.model.RequestMetadata;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import java.io.IOException;

@Log4j2
@Component
public class RequestMetadataFilter implements Filter {

    @Autowired
    private RequestMetadataRepository metadataRepository;

    @Autowired
    private DataWebSocketHandler dataWebSocketHandler;

    @Autowired
    private RedisService redisService;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        try {
            // Extract metadata
            String sourceIp = httpRequest.getRemoteAddr();
            String requestUrl = httpRequest.getRequestURI();
            String ec2InstanceName = getEc2InstanceName();
            RequestMetadata metadata = new RequestMetadata();
            metadata.setSourceIp(sourceIp);
            metadata.setEc2InstanceName(ec2InstanceName);
            metadata.setRequestUrl(requestUrl);
            metadata.setStatus("received");

            // Save metadata to the database
            metadataRepository.save(metadata);
            log.info("Metadata saved: " + metadata.toString());

            // Save metadata to Redis with both main and secondary keys
            //String mainKey = System.getenv("WORKER_NAME");
            long timestamp = metadata.getCreatedAt();
            String secondaryKey = String.valueOf(timestamp); // Use timestamp or a unique ID
            // Update metadata in Redis
            redisService.saveValue("observer", secondaryKey, metadata.toJsonString());


            // Proceed with the request
            chain.doFilter(request, response);

            // Update status to 'responded' after the response is returned
            //metadata.setStatus("responded");
            //metadataRepository.save(metadata);
            log.info("Metadata updated to responded: " + metadata.toJsonString());
            redisService.saveValue("observer", secondaryKey, metadata.toJsonString());



        } catch (Exception e) {
            log.error("Error in RequestMetadataFilter: ", e);
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {
    }

    private String getEc2InstanceName() {
        // Implement logic to retrieve EC2 instance name
        // This can be achieved by querying instance metadata service or using AWS SDK
        return System.getenv("WORKER_NAME");
    }
}