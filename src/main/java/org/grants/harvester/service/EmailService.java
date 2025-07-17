package org.grants.harvester.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.grants.harvester.dto.GrantPlanDTO;
import org.grants.harvester.dto.PlanDTO;
import org.grants.harvester.entity.ProfPlan;
import org.grants.harvester.entity.Subscription;
import org.grants.harvester.repository.ProfPlanRepository;
import org.grants.harvester.repository.SubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class EmailService {

    @Autowired
    private SubscriptionRepository subscriptionRepo;

    @Autowired
    private ProfPlanRepository profPlanRepo;

    @Autowired
    private PipelineService pipeline;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${spring.mail.username}")
    private String fromAddr;

    /**
     * Every Monday at 05:00 UTC send out emails.
     */
    @Scheduled(cron = "0 0 5 * * MON")
    public void sendWeekly() {
        List<Subscription> subs = subscriptionRepo.findAll().stream()
                .filter(Subscription::isEnabled)
                .collect(Collectors.toList());

        Map<String, List<String>> emailsBySlug = subs.stream()
                .collect(Collectors.groupingBy(
                        Subscription::getSlug,
                        Collectors.mapping(Subscription::getEmail, Collectors.toList())
                ));

        for (var entry : emailsBySlug.entrySet()) {
            String slug = entry.getKey();
            List<String> emails = entry.getValue();

            List<GrantPlanDTO> top3 = pipeline.generatePlansForProfessor(slug).stream()
                    .sorted(Comparator.comparingDouble(GrantPlanDTO::fitScore).reversed())
                    .limit(3)
                    .toList();

            List<GrantPlanDTO> cachedTop3 = new ArrayList<>();
            for (GrantPlanDTO grant : top3) {
                String oppNo = grant.link(); // or use grant.oppNo()
                ProfPlan cached = profPlanRepo.findByIdProfSlugAndIdOppNo(slug, oppNo);
                List<PlanDTO> planList;
                if (cached != null) {
                    try {
                        planList = objectMapper.readValue(cached.getPlanJson(), new TypeReference<List<PlanDTO>>() {});
                    } catch (Exception e) {
                        planList = grant.plans();
                    }
                } else {
                    planList = grant.plans();
                    try {
                        String json = objectMapper.writeValueAsString(planList);
                        profPlanRepo.save(new ProfPlan(slug, oppNo, json));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                cachedTop3.add(new GrantPlanDTO(
                        grant.title(),
                        grant.fitScore(),
                        grant.link(),
                        planList
                ));
            }

            String profName = slug.replace("-", " ");
            for (String to : emails) {
                try {
                    sendEmail(to, profName, cachedTop3);
                    System.out.println("Sent grant-match email to " + to);
                } catch (MessagingException e) {
                    System.err.println("Failed to send to " + to + ": " + e.getMessage());
                }
            }
        }
    }

    private void sendEmail(String to, String profName, List<GrantPlanDTO> plans) throws MessagingException {
        MimeMessage msg = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
        helper.setFrom(fromAddr);
        helper.setTo(to);
        helper.setSubject("Your Top Grant Matches for " + profName);
        helper.setText(buildPlain(profName, plans), buildHtml(profName, plans));
        mailSender.send(msg);
    }

    private String buildPlain(String prof, List<GrantPlanDTO> plans) {
        StringBuilder sb = new StringBuilder();
        sb.append("Hi ").append(prof).append(",\n\n");
        sb.append("Here are your top grant matches:\n\n");
        for (GrantPlanDTO g : plans) {
            sb.append(g.title())
                    .append(" (Score: ").append(g.fitScore()).append(")\n")
                    .append(g.link()).append("\n");
            for (PlanDTO plan : g.plans()) {
                sb.append("- ").append(plan.rationale()).append("\n");
                for (String step : plan.steps()) {
                    sb.append("    • ").append(step).append("\n");
                }
            }
            sb.append("\n");
        }
        sb.append("Reply to this email with any questions.\n");
        return sb.toString();
    }

    private String buildHtml(String prof, List<GrantPlanDTO> plans) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body>")
                .append("<h2>Hi ").append(prof).append(",</h2>")
                .append("<p>Here are your <strong>top grant matches</strong>:</p>");
        for (GrantPlanDTO g : plans) {
            sb.append("<div style='margin-bottom:18px;padding:10px;border:1px solid #eee;border-radius:8px;'>")
                    .append("<h3 style='margin:0;'><a href='").append(g.link())
                    .append("' target='_blank'>").append(g.title()).append("</a>")
                    .append(" <span style='font-size:small;color:#888;'>(Score: ")
                    .append(g.fitScore()).append(")</span></h3>");
            for (PlanDTO plan : g.plans()) {
                sb.append("<details><summary>")
                        .append(plan.rationale())
                        .append("</summary><ul>");
                for (String step : plan.steps()) {
                    sb.append("<li>").append(step).append("</li>");
                }
                sb.append("</ul></details>");
            }
            sb.append("</div>");
        }
        sb.append("<p style='margin-top:24px;'>Reply to this email with any questions.</p>")
                .append("</body></html>");
        return sb.toString();
    }
}
