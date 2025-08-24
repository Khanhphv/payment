package payment_gateways.payment.dto;

import lombok.Data;

@Data
public class CryptoClouldCreate {
  private String amount;
  private String email;
  private String service;
}
