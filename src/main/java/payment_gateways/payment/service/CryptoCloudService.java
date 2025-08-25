package payment_gateways.payment.service;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import payment_gateways.payment.dto.CryptoClouldCreate;
import payment_gateways.payment.contants.InvoiceStatus;
import payment_gateways.payment.contants.PaymentMethod;
import payment_gateways.payment.dto.CryotoCloudResponse;
import payment_gateways.payment.repository.InvoiceRepository;
import payment_gateways.payment.interfaces.InvoiceInterface;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import payment_gateways.payment.model.Invoice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.math.BigDecimal;

@Service("cryptocloudService")
public class CryptoCloudService implements InvoiceInterface<CryptoClouldCreate> {

  @Value("${cryptocloud.api.url}")
  private String apiUrl;

  @Value("${cryptocloud.api.key}")
  private String apiKey;

  @Value("${cryptocloud.api.shop_id}")
  private String shopId;

  @Autowired
  private InvoiceRepository invoiceRepository;

  @Autowired
  private EmailService emailService;

  String CURRENCY = "USD";

  String[] ACCEPTED_CURRENCIES = { "USDT_TRC20", "ETH", "BTC" };

  private final ObjectMapper objectMapper = new ObjectMapper();

  private static final Logger logger = LoggerFactory.getLogger(CryptoCloudService.class);
  private final CloseableHttpClient httpClient = HttpClients.createDefault();

  public Invoice createInvoice(CryptoClouldCreate payload) {
    logger.info("Creating crypto cloud invoice: {}", payload);
    try {
      CryotoCloudResponse response = createCryptoCloudInvoice(payload);
      logger.info("CryptoCloud API response: {}", response);
      Invoice invoice = new Invoice();
      invoice.setInvoiceNumber(UUID.randomUUID().toString());
      invoice.setAmount(BigDecimal.valueOf(response.getResult().getAmount()));
      invoice.setCurrency(CURRENCY);
      invoice.setCurrency2(CURRENCY);
      invoice.setEmail(payload.getEmail());
      invoice.setDescription("");
      invoice.setPaymentMethod(PaymentMethod.CRYPTOCLOUD);
      invoice.setPaymentUrl(response.getResult().getLink());
      invoice.setStatus(InvoiceStatus.CREATED);
      invoice.setService(payload.getService());
      invoice.setLogs(response.toString());
      invoiceRepository.save(invoice);
      return invoice;
    } catch (Exception e) {
      logger.error("{}", e.getStackTrace());
      throw new RuntimeException(e.getMessage());
    }

  }

  public String getTransactionInfo(String id) throws Exception {
    return "Transaction info";
  }

  public void verifyInvoice(Map<String, String> bodyMap) {
    try {

      logger.info("Verify invoice: {}", bodyMap);
      String invoiceId = bodyMap.get("invoice_id");
      String status = bodyMap.get("status");
      if (status.equals("success")) {
        Optional<Invoice> invoice = invoiceRepository.findByInvoiceNumber(invoiceId);
        if (invoice.isPresent()) {
          invoice.get().setStatus(InvoiceStatus.COMPLETED);
          emailService.sendInvoiceEmail(invoice.get());
          invoiceRepository.save(invoice.get());
        }
      }

    } catch (Exception e) {
      logger.error("{}", e.getStackTrace());
      throw new RuntimeException(e);
    }

  }

  private HttpPost createHttpRequest(String jsonBody) throws Exception {
    HttpPost request = new HttpPost(apiUrl + "/invoice/create");
    request.setHeader("Authorization", "Token " + apiKey);
    request.setHeader("Content-Type", "application/json");
    request.setEntity(new StringEntity(jsonBody, "UTF-8"));
    return request;
  }

  private CryotoCloudResponse createCryptoCloudInvoice(CryptoClouldCreate payload) throws Exception {

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("shop_id", shopId);
    requestBody.put("amount", payload.getAmount());
    requestBody.put("currency", CURRENCY);
    requestBody.put("available_currencies", ACCEPTED_CURRENCIES);
    String jsonBody = objectMapper.writeValueAsString(requestBody);
    HttpPost request = createHttpRequest(jsonBody);
    try (CloseableHttpResponse response = httpClient.execute(request)) {
      String responseBody = EntityUtils.toString(response.getEntity());
      logger.info("CryptoCloud API response: {}", responseBody);
      if (response.getStatusLine().getStatusCode() == 200) {
        return objectMapper.readValue(responseBody, CryotoCloudResponse.class);
      } else {
        throw new Exception("Failed to create crypto cloud invoice");
      }
    }
  }

}
