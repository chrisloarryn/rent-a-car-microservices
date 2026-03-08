package com.kodlamaio.paymentservice.business.dto.requests.create;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreatePaymentRequest
{
    @NotBlank(message = "Card number cant be empty...")
    @Length(min=16, max=16, message = "The card number must have a length of 16...")
    private String cardNumber;
    @NotBlank(message = "Card holder cant be empty...")
    @Length(min=5, message = "The card holder must have a minimum length of 5...")
    private String cardHolder;

    @Min(value = 2024, message = "Card expiration year must be 2024 or later.")
    private int cardExpirationYear;

    @Max(value = 12, message = "Card expiration month must be between 1 and 12.")
    @Min(value = 1, message = "Card expiration month must be between 1 and 12.")
    private int cardExpirationMonth;

    @NotBlank(message = "Card CVV cant be empty...")
    @Length(min=3, max = 3, message = "The card CVV numbert must have a length of 3...")
    private String cardCvv;
    @DecimalMin(value = "0.01", message = "Balance must be greater than 0.")
    private double balance;
}
