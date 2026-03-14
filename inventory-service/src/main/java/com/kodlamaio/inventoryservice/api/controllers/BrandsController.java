package com.kodlamaio.inventoryservice.api.controllers;

import com.kodlamaio.commonpackage.utils.constants.Roles;
import com.kodlamaio.commonpackage.utils.dto.responses.PageResponse;
import com.kodlamaio.inventoryservice.business.abstracts.BrandService;
import com.kodlamaio.inventoryservice.business.dto.requests.create.CreateBrandRequest;
import com.kodlamaio.inventoryservice.business.dto.requests.update.UpdateBrandRequest;
import com.kodlamaio.inventoryservice.business.dto.responses.create.CreateBrandResponse;
import com.kodlamaio.inventoryservice.business.dto.responses.get.GetAllBrandsResponse;
import com.kodlamaio.inventoryservice.business.dto.responses.get.GetBrandResponse;
import com.kodlamaio.inventoryservice.business.dto.responses.update.UpdateBrandResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/api/brands")
@Validated
@Tag(name = "Brands", description = "Inventory brand operations")
public class BrandsController {
    private final BrandService service;

    @GetMapping
    @PreAuthorize(Roles.UserOrAbove)
    public PageResponse<GetAllBrandsResponse> getAll(@PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return service.getAll(pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize(Roles.UserOrAbove)
    public GetBrandResponse getById(@PathVariable @NotNull UUID id) {
        return service.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Roles.AdminOrModerator)
    public CreateBrandResponse add(@Valid @RequestBody CreateBrandRequest request) {
        return service.add(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize(Roles.AdminOrModerator)
    public UpdateBrandResponse update(@PathVariable @NotNull UUID id, @Valid @RequestBody UpdateBrandRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(Roles.AdminOrModerator)
    public void delete(@PathVariable @NotNull UUID id) {
        service.delete(id);
    }
}
