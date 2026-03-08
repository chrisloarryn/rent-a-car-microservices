package com.kodlamaio.maintenanceservice.business.dto.requests.create;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CreateMaintenanceRequest
{
    @NotBlank(message = "Maintenance information is required.")
    @Size(min = 5, max = 255, message = "Maintenance information must contain between 5 and 255 characters.")
    private String information;
    @NotNull(message = "Car id is required.")
    private UUID carId;
}
