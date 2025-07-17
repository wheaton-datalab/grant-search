package org.grants.harvester.controller;

import org.grants.harvester.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DevController {

    @Autowired
    private EmailService emailService;

    /**
     * Dev‐only endpoint to fire the weekly send() immediately.
     * Not for production use.
     */
    @PostMapping("/api/dev/run-cron")
    public String runCronNow() {
        emailService.sendWeekly();
        return "Cron triggered!!!";
    }
}
