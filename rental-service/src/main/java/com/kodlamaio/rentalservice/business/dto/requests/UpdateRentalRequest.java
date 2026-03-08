package com.kodlamaio.rentalservice.business.dto.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRentalRequest
{
    @NotNull(message = "Car id is required.")
    private UUID carId;
    @Min(value = 1, message = "Daily price must be at least 1.")
    private double dailyPrice;
    @Min(value = 1, message = "Rented days must be at least 1.")
    private int rentedForDays;
    @NotNull(message = "Rental date is required.")
    private LocalDate rentedAt;
}
