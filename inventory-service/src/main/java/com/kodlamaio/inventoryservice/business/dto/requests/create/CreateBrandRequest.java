package com.kodlamaio.inventoryservice.business.dto.requests.create;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateBrandRequest {
    @NotBlank(message = "Brand name is required.")
    @Size(min = 2, max = 20, message = "Brand name must contain between 2 and 20 characters.")
    private String name;
}
