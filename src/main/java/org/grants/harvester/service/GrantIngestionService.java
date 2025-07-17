package org.grants.harvester.service;

import java.util.List;
import java.util.Map;

import org.grants.harvester.entity.RawGrant;
import org.grants.harvester.repository.RawGrantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Ingests new or updated grants from Grants.gov POST /v1/api/search2
 */
@Service
public class GrantIngestionService {
    private static final Logger log = LoggerFactory.getLogger(GrantIngestionService.class);

    private final RawGrantRepository repo;
    private final RestTemplate rest;

    public GrantIngestionService(RawGrantRepository repo,
                                 RestTemplateBuilder builder) {
        this.repo = repo;
        this.rest = builder.build();
    }

    /**
     * Stub schedule; call manually or adjust cron as needed.
     */
    @Scheduled(cron = "0 0 4 * * MON")
    @Transactional
    public void ingestWeekly() {
        try {
            String url = "https://api.grants.gov/v1/api/search2";
            log.info("POST to Grants.gov search2 at {}", url);

            // Required request body per docs
            Map<String,Object> payload = Map.of(
                "rows",               500,
                "keyword",            "",
                "oppNum",             "",
                "eligibilities",      "",
                "agencies",           "",
                "oppStatuses",        "posted|forecasted",
                "aln",                "",
                "fundingCategories",  ""
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String,Object>> req = new HttpEntity<>(payload, headers);

            ResponseEntity<GrantsApiResponse> resp = rest.exchange(
                url, HttpMethod.POST, req, GrantsApiResponse.class
            );

            if (resp.getStatusCode() != HttpStatus.OK || resp.getBody() == null) {
                throw new IllegalStateException("Bad response: " + resp.getStatusCode());
            }

            List<GrantHit> hits = resp.getBody().data.oppHits;
            log.info("Received {} opportunities", hits.size());

            for (GrantHit hit : hits) {
                RawGrant g = new RawGrant(
                    hit.getNumber(),    // opportunity number
                    hit.getTitle(),
                    hit.getSynopsis(),  // use synopsisDesc or similar
                    hit.getOppStatus(),
                    hit.getSynopsisUrl() // or link field if provided
                );
                repo.findById(g.getOppNo())
                    .map(existing -> {
                        if (!existing.getStatus().equals(g.getStatus())) {
                            existing.setStatus(g.getStatus());
                            repo.save(existing);
                        }
                        return existing;
                    })
                    .orElseGet(() -> repo.save(g));
            }

        } catch (Exception e) {
            log.error("Error during ingestion", e);
            throw e;
        }
    }

    // ─── Mapping classes ────────────────────────────────────────

    /** Top‐level response */
    static class GrantsApiResponse {
        @JsonProperty("data")
        public GrantsData data;
    }

    /** The `data` object contains `oppHits` */
    static class GrantsData {
        @JsonProperty("oppHits")
        public List<GrantHit> oppHits;
    }

    /** Each hit in the results */
    static class GrantHit {
        @JsonProperty("number")       private String number;
        @JsonProperty("title")        private String title;
        @JsonProperty("synopsis")     private Synopsis synopsis;
        @JsonProperty("oppStatus")    private String oppStatus;
        // synopsis object carries fields like synopsisDesc and URL:
        static class Synopsis {
            @JsonProperty("synopsisDesc") private String synopsisDesc;
            @JsonProperty("synopsisDocumentURLs")
            private List<String> documentUrls;
            public String getSynopsisDesc() { return synopsisDesc; }
            public String firstUrl() {
                return (documentUrls != null && !documentUrls.isEmpty())
                     ? documentUrls.get(0) : null;
            }
        }

        public GrantHit() {}

        public String getNumber() { return number; }
        public String getTitle() { return title; }
        public String getSynopsis() {
            return synopsis != null ? synopsis.getSynopsisDesc() : "";
        }
        public String getSynopsisUrl() {
            return synopsis != null ? synopsis.firstUrl() : null;
        }
        public String getOppStatus() { return oppStatus; }
    }
}
