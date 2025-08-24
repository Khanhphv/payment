package payment_gateways.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class CryotoCloudResponse {
  private String status;
  private Result result;

  @Data
  public static class Result {
    private String uuid;
    private String created;
    private String address;
    @JsonProperty("expiry_date")
    private String expiryDate;
    @JsonProperty("side_commission")
    private String sideCommission;
    @JsonProperty("side_commission_service")
    private String sideCommissionService;
    @JsonProperty("type_payments")
    private String typePayments;
    private double amount;
    @JsonProperty("amount_usd")
    private double amountUsd;
    @JsonProperty("amount_in_fiat")
    private double amountInFiat;
    private double fee;
    @JsonProperty("fee_usd")
    private double feeUsd;
    @JsonProperty("service_fee")
    private double serviceFee;
    @JsonProperty("service_fee_usd")
    private double serviceFeeUsd;
    @JsonProperty("fiat_currency")
    private String fiatCurrency;
    private String status;
    @JsonProperty("is_email_required")
    private boolean isEmailRequired;
    private String link;
    @JsonProperty("invoice_id")
    private String invoiceId;
    private Currency currency;
    private Project project;
    @JsonProperty("test_mode")
    private boolean testMode;
  }

  @Data
  public static class Currency {
    private int id;
    private String code;
    private String fullcode;
    private Network network;
    private String name;
    @JsonProperty("is_email_required")
    private boolean isEmailRequired;
    private boolean stablecoin;
    @JsonProperty("icon_base")
    private String iconBase;
    @JsonProperty("icon_network")
    private String iconNetwork;
    @JsonProperty("icon_qr")
    private String iconQr;
    private int order;
  }

  @Data
  public static class Network {
    private String code;
    private int id;
    private String icon;
    private String fullname;
  }

  @Data
  public static class Project {
    private int id;
    private String name;
    private String fail;
    private String success;
    private String logo;
  }
}
