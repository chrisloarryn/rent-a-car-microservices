package com.kodlamaio.rentalservice.business.dto.requests;

import com.kodlamaio.commonpackage.utils.constants.Regex;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateRentalRequest
{
    @NotNull(message = "Car id is required.")
    private UUID carId;
    @Min(value = 1, message = "Daily price must be at least 1.")
    private double dailyPrice;
    @Min(value = 1, message = "Rented days must be at least 1.")
    private int rentedForDays;

    @NotBlank(message = "Card number is required.")
    @Pattern(regexp = Regex.CardNumber, message = "Card number must contain exactly 16 digits.")
    private String cardNumber;
    @NotBlank(message = "Card holder is required.")
    private String cardHolder;
    @Min(value = 2024, message = "Card expiration year must be 2024 or later.")
    private int cardExpirationYear;
    @Min(value = 1, message = "Card expiration month must be between 1 and 12.")
    @Max(value = 12, message = "Card expiration month must be between 1 and 12.")
    private int cardExpirationMonth;
    @NotBlank(message = "Card CVV is required.")
    @Pattern(regexp = Regex.CardCvv, message = "Card CVV must contain exactly 3 digits.")
    private String cardCvv;
}
