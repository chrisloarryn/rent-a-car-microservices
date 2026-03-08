package com.kodlamaio.inventoryservice.business.dto.requests.create;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateModelRequest {
    @NotNull(message = "Brand id is required.")
    private UUID brandId;
    @NotBlank(message = "Model name is required.")
    @Size(min = 2, max = 20, message = "Model name must contain between 2 and 20 characters.")
    private String name;
}
