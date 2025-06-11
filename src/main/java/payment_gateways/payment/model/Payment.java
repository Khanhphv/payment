package payment_gateways.payment.model;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Payment {
  private Long id;
  private String paymentMethod;
  private BigDecimal amount;
  private String currency;
  private String status;
  private LocalDateTime createdAt;
  private String description;
}