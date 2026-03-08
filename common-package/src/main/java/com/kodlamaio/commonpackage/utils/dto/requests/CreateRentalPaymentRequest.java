package com.kodlamaio.commonpackage.utils.dto.requests;

import com.kodlamaio.commonpackage.utils.constants.Regex;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreateRentalPaymentRequest
{
    @NotBlank(message = "Card number is required.")
    @Length(min = 16, max = 16, message = "Card number must contain exactly 16 digits.")
    @jakarta.validation.constraints.Pattern(regexp = Regex.CardNumber, message = "Card number must contain only digits.")
    private String cardNumber;

    @NotBlank(message = "Card holder is required.")
    @Length(min = 5, max = 100, message = "Card holder must contain between 5 and 100 characters.")
    private String cardHolder;

    @Min(value = 2024, message = "Card expiration year must be 2024 or later.")
    private int cardExpirationYear;

    @Min(value = 1, message = "Card expiration month must be between 1 and 12.")
    @Max(value = 12, message = "Card expiration month must be between 1 and 12.")
    private int cardExpirationMonth;

    @NotBlank(message = "Card CVV is required.")
    @Length(min = 3, max = 3, message = "Card CVV must contain exactly 3 digits.")
    @jakarta.validation.constraints.Pattern(regexp = Regex.CardCvv, message = "Card CVV must contain only digits.")
    private String cardCvv;

    @DecimalMin(value = "0.01", message = "Payment price must be greater than 0.")
    private double price;
}
