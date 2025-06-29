package payment_gateways.payment.model;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {
  private boolean success;
  private String message;
  private T data;
  private String error;

  public static <T> ApiResponse<T> success(T data) {
    return new ApiResponse<>(true, "Success", data, null);
  }

  public static <T> ApiResponse<T> success(T data, String message) {
    return new ApiResponse<>(true, message, data, null);
  }

  public static <T> ApiResponse<T> error(String error) {
    return new ApiResponse<>(false, null, null, error);
  }

  public static <T> ApiResponse<T> error(String message, String error) {
    return new ApiResponse<>(false, message, null, error);
  }
}