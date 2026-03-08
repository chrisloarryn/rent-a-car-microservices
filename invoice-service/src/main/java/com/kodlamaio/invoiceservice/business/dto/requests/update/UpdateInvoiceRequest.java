package com.kodlamaio.invoiceservice.business.dto.requests.update;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UpdateInvoiceRequest
{
    @NotBlank(message = "Card holder is required.")
    private String cardHolder;
    @NotBlank(message = "Model name is required.")
    private String modelName;
    @NotBlank(message = "Brand name is required.")
    private String brandName;
    @NotBlank(message = "Plate is required.")
    private String plate;
    @Min(value = 1996, message = "Model year must be 1996 or later.")
    private int modelYear;
    @DecimalMin(value = "0.01", message = "Daily price must be greater than 0.")
    private double dailyPrice;
    @Min(value = 1, message = "Rented days must be at least 1.")
    private int rentedForDays;
    @NotNull(message = "Rental timestamp is required.")
    private LocalDateTime rentedAt;
}
