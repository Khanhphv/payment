package payment_gateways.payment.model;

import lombok.Data;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import payment_gateways.payment.contants.InvoiceStatus;
import payment_gateways.payment.contants.PaymentMethod;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

@Data
@Document(collection = "invoices")
public class Invoice {
  @Id
  private String id;

  @Indexed(unique = true)
  private String invoiceNumber;

  @NotNull(message = "Amount is required")
  @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
  private BigDecimal amount;

  @NotBlank(message = "Currency is required")
  private String currency;

  @NotBlank(message = "Currency2 is required")
  private String currency2;

  private PaymentMethod paymentMethod;

  private String paymentUrl;

  @Email(message = "Invalid email address")
  @NotBlank(message = "Email is required")
  @Indexed
  private String email;

  private InvoiceStatus status;

  private LocalDateTime createdAt = LocalDateTime.now(ZoneOffset.UTC);

  @Size(max = 500, message = "Description cannot exceed 500 characters")
  private String description;

  private String successUrl;

  private String cancelUrl;

  private String ipnUrl;

  public void setPaymentMethod(PaymentMethod paymentMethod) {
    this.paymentMethod = paymentMethod;
  }

  public PaymentMethod getPaymentMethod() {
    return paymentMethod;
  }

}
