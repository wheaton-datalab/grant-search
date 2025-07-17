package org.grants.harvester.service;

import java.util.*;
import java.util.stream.Collectors;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.grants.harvester.dto.GrantPlanDTO;
import org.grants.harvester.entity.Subscription;
import org.grants.harvester.repository.SubscriptionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final SubscriptionRepository subscriptionRepo;
    private final PipelineService pipeline;      // your existing service
    private final JavaMailSender mailSender;
    private final String fromAddr;

    public EmailService(SubscriptionRepository subscriptionRepo,
                        PipelineService pipeline,
                        JavaMailSender mailSender,
                        @Value("${spring.mail.username}") String fromAddr) {
        this.subscriptionRepo = subscriptionRepo;
        this.pipeline         = pipeline;
        this.mailSender       = mailSender;
        this.fromAddr         = fromAddr;
    }

    /**
     * Every Monday at 05:00 UTC send out emails.
     */
    @Scheduled(cron = "0 0 5 * * MON")
    public void sendWeekly() {
        // 1) load all enabled subscriptions
        List<Subscription> subs = subscriptionRepo.findAll()
            .stream()
            .filter(Subscription::isEnabled)
            .collect(Collectors.toList());

        // 2) group by slug
        Map<String, List<String>> emailsBySlug = subs.stream()
            .collect(Collectors.groupingBy(
                Subscription::getSlug,
                Collectors.mapping(Subscription::getEmail, Collectors.toList())
            ));

        // 3) for each slug, fetch top 3 plans and email each user
        emailsBySlug.forEach((slug, emails) -> {
            // get plans
            List<GrantPlanDTO> plans = pipeline.generatePlansForProfessor(slug).stream()
                .sorted(Comparator.comparingDouble(GrantPlanDTO::fitScore).reversed())
                .limit(3)
                .toList();

            String profName = slug.replace("-", " ");
            emails.forEach(email -> {
                try {
                    sendEmail(email, profName, plans);
                } catch (MessagingException e) {
                    // log and continue
                    System.err.println("Failed to send to " + email + ": " + e.getMessage());
                }
            });
        });
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
              .append(g.link()).append("\n\n");
        }
        sb.append("Reply to this email with any questions.\n");
        return sb.toString();
    }

    private String buildHtml(String prof, List<GrantPlanDTO> plans) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body>")
          .append("<h2>Hi ").append(prof).append(",</h2>")
          .append("<p>Here are your top grant matches:</p>");
        for (GrantPlanDTO g : plans) {
            sb.append("<div style='margin-bottom:16px;'>")
              .append("<h3><a href='").append(g.link())
              .append("'>").append(g.title())
              .append("</a> (Score: ").append(g.fitScore()).append(")</h3>")
              .append("</div>");
        }
        sb.append("<p>Reply to this email with any questions.</p>")
          .append("</body></html>");
        return sb.toString();
    }
}
