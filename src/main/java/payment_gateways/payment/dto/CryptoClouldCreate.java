package payment_gateways.payment.dto;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;

@Data
@Schema(description = "CryptoCloud create invoice")
public class CryptoClouldCreate {
  @Schema(description = "Amount", required = true, allowableValues = { "100", "400" })
  private String amount;
  @Schema(description = "Email", required = true, format = "email")
  @Email(message = "Invalid email address")
  private String email;
  @Schema(description = "Service", required = true)
  private String service;
}
