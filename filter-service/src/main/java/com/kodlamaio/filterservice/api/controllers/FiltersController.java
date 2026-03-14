package com.kodlamaio.filterservice.api.controllers;

import com.kodlamaio.commonpackage.utils.constants.Roles;
import com.kodlamaio.commonpackage.utils.dto.responses.PageResponse;
import com.kodlamaio.filterservice.business.abstracts.FilterService;
import com.kodlamaio.filterservice.business.dto.responses.GetAllFiltersResponse;
import com.kodlamaio.filterservice.business.dto.responses.GetFilterResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/api/filters")
@Validated
@Tag(name = "Filters", description = "Filter query operations")
public class FiltersController
{
    private final FilterService service;

    @GetMapping
    @PreAuthorize(Roles.UserOrAbove)
    public PageResponse<GetAllFiltersResponse> getAll(@PageableDefault(size = 20, sort = "id") Pageable pageable)
    {
        return service.getAll(pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize(Roles.UserOrAbove)
    public GetFilterResponse getById(@PathVariable @NotNull UUID id)
    {
        return service.getById(id);
    }

}
