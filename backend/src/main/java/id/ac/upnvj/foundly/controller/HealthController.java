package id.ac.upnvj.foundly.controller;

import id.ac.upnvj.foundly.dto.HealthResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Endpoint awal untuk memastikan backend dapat menerima request HTTP. */
@RestController
public class HealthController {

    @GetMapping("/api/health")
    public HealthResponse health() {
        return new HealthResponse("Foundly", "UP", "Backend Foundly berjalan.");
    }
}
