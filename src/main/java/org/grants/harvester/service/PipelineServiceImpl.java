package org.grants.harvester.service;

import java.util.List;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.grants.harvester.dto.GrantPlanDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.context.annotation.Primary;       

@Primary                                                 
@Service
public class PipelineServiceImpl implements PipelineService {
    private static final Logger log = LoggerFactory.getLogger(PipelineServiceImpl.class);

    private final RestTemplate rest;
    private final String matchServiceBaseUrl;

    public PipelineServiceImpl(
            RestTemplate restTemplate,
            @Value("${grant.match.service.url:http://127.0.0.1:5000}") String baseUrl
    ) {
        this.rest = restTemplate;
        this.matchServiceBaseUrl = baseUrl;
    }

    @Override
    public List<GrantPlanDTO> generatePlansForProfessor(String slug) {
        log.info("Calling match-service for slug={}", slug);
        String url = matchServiceBaseUrl + "/match?slug={slug}";
        GrantPlanDTO[] response = rest.getForObject(
            url,
            GrantPlanDTO[].class,
            slug
        );
        return (response != null)
             ? Arrays.asList(response)
             : List.of();
    }
}
