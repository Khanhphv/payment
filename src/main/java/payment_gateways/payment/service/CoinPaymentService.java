package payment_gateways.payment.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import payment_gateways.payment.contants.InvoiceStatus;
import payment_gateways.payment.contants.PaymentMethod;
import payment_gateways.payment.interfaces.InvoiceInterface;
import payment_gateways.payment.model.Invoice;
import payment_gateways.payment.repository.InvoiceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.entity.StringEntity;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

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

  @Autowired
  private InvoiceRepository invoiceRepository;

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
      String invoiceNumber = UUID.randomUUID().toString();
      data.setInvoiceNumber(invoiceNumber);
      params.put("version", "1");
      params.put("key", apiKey);
      params.put("cmd", "create_transaction");
      params.put("amount", "0.012");
      params.put("currency1", "LTC");
      params.put("currency2", "LTC");
      params.put("buyer_email", data.getEmail()); // You might want to add this to Invoice model
      params.put("item_name", data.getService());
      params.put("item_number", data.getInvoiceNumber());
      params.put("ipn_url", java.net.URLEncoder.encode(ipnUrl, StandardCharsets.UTF_8));
      params.put("success_url", data.getSuccessUrl());
      params.put("cancel_url", data.getCancelUrl());
      params.put("want_shipping", "0");
      params.put("txn_id", invoiceNumber);

      String payload = params.entrySet().stream()
          .map(entry -> entry.getKey() + "=" + entry.getValue())
          .collect(Collectors.joining("&"));
      Mac mac = Mac.getInstance("HmacSHA512");
      SecretKeySpec secretKeySpec = new SecretKeySpec(apiSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
      mac.init(secretKeySpec);
      String hmac = bytesToHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
      logger.info("CoinPayment API request hmac: {}", hmac);
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

        String txid = (String) result.get("txid");
        data.setPaymentUrl((String) result.get("checkout_url"));
        data.setStatus(InvoiceStatus.CREATED);
        data.setPaymentMethod(PaymentMethod.COINPAYMENT);
        data.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));
        data.setLogs(responseBody);
        data.setInvoiceNumber(txid);
        invoiceRepository.save(data);

        Invoice invoice = new Invoice();

        invoice.setInvoiceNumber(data.getInvoiceNumber());
        invoice.setAmount(data.getAmount());
        invoice.setCurrency(data.getCurrency());
        invoice.setCurrency2(data.getCurrency2());
        invoice.setEmail(data.getEmail());
        invoice.setDescription(data.getDescription());
        invoice.setPaymentUrl(data.getPaymentUrl());
        return invoice;
      }
    } catch (Exception e) {
      logger.error("Failed to create CoinPayment transaction: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to create CoinPayment transaction: " + e.getMessage());
    }
  }

  @Override
  public void verifyInvoice(Map<String, String> bodyMap) {
    String id = bodyMap.get("item_number");
    String status = bodyMap.get("status");
    String email = bodyMap.get("email");
    String service = bodyMap.get("item_name");
    if (status.equals("100") && id != null && email != null && service != null) {
      Optional<Invoice> invoice = invoiceRepository.findByInvoiceNumber(id);
      if (invoice.isPresent()) {

        try {
          invoice.get().setStatus(InvoiceStatus.COMPLETED);
          invoiceRepository.save(invoice.get());
          KeyService keyService = new KeyService();
          keyService.generateLicenseWithService(service);
          EmailService emailService = new EmailService();
          emailService.sendSimpleEmail(email, "CoinPayment Transaction Completed",
              "Your CoinPayment transaction has been completed. Your license key is: "
                  + keyService.generateLicenseWithService(service));
        } catch (Exception e) {
          logger.error("Failed to get transaction info: {}", e.getMessage(), e);
          invoice.get().setStatus(InvoiceStatus.FAILED);
          invoiceRepository.save(invoice.get());
        }
      }
    }
  }

  public String getTransactionInfo(String txid) throws Exception {
    Map<String, String> params = new LinkedHashMap<>();
    params.put("version", "1");
    params.put("cmd", "get_tx_info");
    params.put("key", apiKey);
    params.put("format", "json");
    params.put("txid", txid);

    String body = getFormUrlEncodedData(params);
    String hmac = generateHmac(body, apiSecret);

    HttpPost post = new HttpPost(apiUrl);
    post.setHeader("Content-Type", "application/x-www-form-urlencoded");
    post.setHeader("HMAC", hmac);
    post.setEntity(new StringEntity(body));

    try (CloseableHttpClient client = HttpClients.createDefault();
        CloseableHttpResponse response = client.execute(post)) {

      return EntityUtils.toString(response.getEntity());
    }
  }

  private String generateHmac(String data, String key) throws Exception {
    SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
    Mac mac = Mac.getInstance("HmacSHA512");
    mac.init(secretKey);
    byte[] hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
    StringBuilder sb = new StringBuilder();
    for (byte b : hmacBytes) {
      sb.append(String.format("%02x", b));
    }
    return sb.toString();
  }

  private String getFormUrlEncodedData(Map<String, String> params) {
    StringBuilder result = new StringBuilder();
    for (Map.Entry<String, String> entry : params.entrySet()) {
      if (result.length() > 0)
        result.append("&");
      result.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
      result.append("=");
      result.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
    }
    return result.toString();
  }

}
