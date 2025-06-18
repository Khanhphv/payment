package payment_gateways.payment.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import payment_gateways.payment.contants.InvoiceStatus;
import payment_gateways.payment.contants.PaymentMethod;
import payment_gateways.payment.interfaces.InvoiceInterface;
import payment_gateways.payment.model.Invoice;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.entity.StringEntity;
import java.util.HashMap;
import java.util.Map;

@Service("nowPaymentService")
public class NowPaymentService implements InvoiceInterface {
  private static final Logger logger = LoggerFactory.getLogger(NowPaymentService.class);

  @Value("${nowpayment.api.key}")
  private String apiKey;

  @Value("${nowpayment.api.url}")
  private String apiUrl;

  @Value("${nowpayment.ipn.url}")
  private String ipnUrl;

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final CloseableHttpClient httpClient = HttpClients.createDefault();

  private Map<String, Object> createRequestBody(Invoice data) {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("price_amount", data.getAmount());
    requestBody.put("price_currency", "USD");
    requestBody.put("order_id", data.getInvoiceNumber());
    requestBody.put("order_description", data.getDescription());
    requestBody.put("ipn_callback_url", ipnUrl);
    requestBody.put("success_url", data.getSuccessUrl());
    requestBody.put("cancel_url", data.getCancelUrl());
    return requestBody;
  }

  private HttpPost createHttpRequest(String jsonBody) throws Exception {
    HttpPost request = new HttpPost(apiUrl + "/invoice");
    request.setHeader("x-api-key", apiKey);
    request.setHeader("Content-Type", "application/json");
    request.setEntity(new StringEntity(jsonBody, "UTF-8"));
    return request;
  }

  private void handleResponse(Invoice data, String responseBody) throws Exception {
    Map<String, Object> responseMap = objectMapper.readValue(responseBody,
        new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {
        });

    data.setPaymentUrl((String) responseMap.get("invoice_url"));
    data.setStatus(InvoiceStatus.CREATED);
    data.setPaymentMethod(PaymentMethod.NOWPAYMENT);
  }

  @Override
  public Invoice createInvoice(Invoice data) {
    try {
      logger.info("Creating NowPayment invoice for order: {}", data.getInvoiceNumber());

      // Prepare and send request
      Map<String, Object> requestBody = createRequestBody(data);
      String jsonBody = objectMapper.writeValueAsString(requestBody);
      logger.info("NowPayment API request body: {}", jsonBody);

      HttpPost request = createHttpRequest(jsonBody);

      // Execute request and handle response
      try (CloseableHttpResponse response = httpClient.execute(request)) {
        String responseBody = EntityUtils.toString(response.getEntity());
        logger.info("NowPayment API response: {}", responseBody);
        handleResponse(data, responseBody);
        return data;
      }
    } catch (Exception e) {
      logger.error("Failed to create NowPayment invoice: {}", e.getMessage(), e);
      data.setStatus(InvoiceStatus.FAILED);
      throw new RuntimeException("Failed to create NowPayment invoice: " + e.getMessage());
    }
  }

  @Override
  public String getTransactionInfo(String id) throws Exception {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getTransactionInfo'");
  }

  @Override
  public void verifyInvoice(Map<String, String> bodyMap) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'verifyInvoice'");
  }
}
