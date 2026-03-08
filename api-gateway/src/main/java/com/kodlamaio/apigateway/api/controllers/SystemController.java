package com.kodlamaio.apigateway.api.controllers;

import com.kodlamaio.commonpackage.utils.dto.responses.SystemPingResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/system")
@Tag(name = "System", description = "Technical API gateway endpoints")
public class SystemController
{
    @GetMapping("/ping")
    public Mono<SystemPingResponse> ping()
    {
        return Mono.just(new SystemPingResponse("api-gateway"));
    }
}
