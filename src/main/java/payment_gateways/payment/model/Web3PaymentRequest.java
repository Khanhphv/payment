package payment_gateways.payment.model;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class Web3PaymentRequest {
  private String txHash;
  private BigDecimal amount;
  private String service;
  private String userAddress;
  private String network;
}