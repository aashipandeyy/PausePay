package dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateAccountRequest {

    @NotBlank(message = "Bank name is required")
    private String bankName;

    @NotBlank(message = "Account label is required")
    private String accountLabel;

    // defaults to inr if not provided
    private String currency = "INR";
}
