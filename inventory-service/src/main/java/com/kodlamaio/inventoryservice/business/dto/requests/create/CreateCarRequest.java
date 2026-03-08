package com.kodlamaio.inventoryservice.business.dto.requests.create;

import com.kodlamaio.commonpackage.utils.annotations.NotFutureYear;
import com.kodlamaio.commonpackage.utils.constants.Regex;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateCarRequest {
    @NotNull(message = "Model id is required.")
    private UUID modelId;
    @Min(value = 2000, message = "Model year must be 2000 or later.")
    @NotFutureYear(message = "Model year cannot be in the future.")
    private int modelYear;
    @NotBlank(message = "Plate is required.")
    @Pattern(regexp = Regex.Plate, message = "Plate must match the format '34 ABC 1234'.")
    private String plate;
    @Min(value = 1, message = "Daily price must be at least 1.")
    private double dailyPrice;
}
