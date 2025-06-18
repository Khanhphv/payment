package payment_gateways.payment.model;

import lombok.Data;
import jakarta.validation.constraints.*;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import payment_gateways.payment.contants.InvoiceStatus;
import payment_gateways.payment.contants.PaymentMethod;

@Data
@Entity
@Table(name = "invoices")
public class Invoice {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true)
  private String invoiceNumber;

  @NotNull(message = "Amount is required")
  @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
  private BigDecimal amount;

  @NotBlank(message = "Currency is required")
  private String currency;

  @NotBlank(message = "Currency2 is required")
  private String currency2;

  @Enumerated(EnumType.STRING)
  private PaymentMethod paymentMethod;

  private String paymentUrl;

  @Email(message = "Invalid email address")
  @NotBlank(message = "Email is required")
  @Column
  private String email;

  @Enumerated(EnumType.STRING)
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

  @Column(columnDefinition = "MEDIUMTEXT")
  private String logs;

  public PaymentMethod getPaymentMethod() {
    return paymentMethod;
  }
}
