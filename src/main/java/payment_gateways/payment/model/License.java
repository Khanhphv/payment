package payment_gateways.payment.model;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class License {
  private List<String> keys;
  private List<LicenseInfo> licenses;
  private String message;
  private boolean success;

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class LicenseInfo {
    private String key;
    private String service;
    private String version;
  }
}
