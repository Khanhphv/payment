package payment_gateways.payment.dto;

import lombok.Data;

@Data
public class CrytoNotify {
  private String invoice_id;
  private String status;
  private String currency;
  private String order_id;
  private String token;
}
