package payment_gateways.payment.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import payment_gateways.payment.contants.InvoiceStatus;
import payment_gateways.payment.contants.PaymentMethod;
import payment_gateways.payment.interfaces.InvoiceInterface;
import payment_gateways.payment.model.Invoice;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.entity.StringEntity;

@Service("coinPaymentService")
public class CoinPaymentService implements InvoiceInterface {
  private static final Logger logger = LoggerFactory.getLogger(CoinPaymentService.class);

  @Value("${coinpayment.api.key}")
  private String apiKey;

  @Value("${coinpayment.api.secret}")
  private String apiSecret;

  @Value("${coinpayment.api.url}")
  private String apiUrl;

  @Value("${coinpayment.ipn.url}")
  private String ipnUrl;

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final CloseableHttpClient httpClient = HttpClients.createDefault();

  private String bytesToHex(byte[] bytes) {
    StringBuilder result = new StringBuilder();
    for (byte b : bytes) {
      result.append(String.format("%02x", b));
    }
    return result.toString();
  }

  @Override
  public Invoice createInvoice(Invoice data) {
    try {
      logger.info("Creating CoinPayment transaction for invoice: {}", data.getInvoiceNumber());

      // Prepare request parameters
      Map<String, String> params = new HashMap<>();
      params.put("version", "1");
      params.put("key", apiKey);
      params.put("cmd", "create_transaction");
      params.put("amount", data.getAmount().toString());
      params.put("currency1", data.getCurrency());
      params.put("currency2", data.getCurrency2());
      params.put("buyer_email", data.getEmail()); // You might want to add this to Invoice model
      params.put("item_name", data.getDescription());
      params.put("item_number", data.getInvoiceNumber());
      params.put("ipn_url", ipnUrl);
      params.put("success_url", data.getSuccessUrl());
      params.put("cancel_url", data.getCancelUrl());
      params.put("want_shipping", "0");
      params.put("txn_id", UUID.randomUUID().toString());

      String payload = params.entrySet().stream()
          .map(entry -> entry.getKey() + "=" + entry.getValue())
          .collect(Collectors.joining("&"));
      Mac mac = Mac.getInstance("HmacSHA512");
      SecretKeySpec secretKeySpec = new SecretKeySpec(apiSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
      mac.init(secretKeySpec);
      String hmac = bytesToHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));

      logger.info("CoinPayment API request payload: {}", payload);
      HttpPost request = new HttpPost(apiUrl);
      request.setHeader("HMAC", hmac);
      request.setHeader("Connection", "close");
      request.setHeader("Accept", "*/*");
      request.setHeader("Content-Type", "application/x-www-form-urlencoded");
      request.setHeader("Cookie2", "$Version=1");
      request.setHeader("Accept-Language", "en-US");

      // Set the form data in the request body
      request.setEntity(new StringEntity(payload, StandardCharsets.UTF_8));

      try (CloseableHttpResponse response = httpClient.execute(request)) {
        String responseBody = EntityUtils.toString(response.getEntity());
        logger.info("CoinPayment API response: {}", responseBody);
        Map<String, Object> responseMap = objectMapper.readValue(responseBody,
            new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {
            });
        logger.info("CoinPayment API response map: {}", responseMap);
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) responseMap.get("result");
        data.setPaymentUrl((String) result.get("checkout_url"));
        data.setStatus(InvoiceStatus.CREATED);
        data.setPaymentMethod(PaymentMethod.COINPAYMENT);

        return data;
      }
    } catch (Exception e) {
      logger.error("Failed to create CoinPayment transaction: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to create CoinPayment transaction: " + e.getMessage());
    }
  }
}
