package com.kodlamaio.discoveryserver.api.controllers;

import com.kodlamaio.commonpackage.utils.dto.responses.SystemPingResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/system")
@Tag(name = "System", description = "Technical discovery server endpoints")
public class SystemController
{
    @GetMapping("/ping")
    public SystemPingResponse ping()
    {
        return new SystemPingResponse("discovery-server");
    }
}
