package payment_gateways.payment.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import payment_gateways.payment.dto.CryptoClouldCreate;
import payment_gateways.payment.interfaces.InvoiceInterface;
import payment_gateways.payment.model.Invoice;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/invoices")
@Tag(name = "Invoice", description = "Invoice management APIs")
public class InvoiceController {

  private static final Logger logger = LoggerFactory.getLogger(InvoiceController.class);
  private final InvoiceInterface<Invoice> nowPaymentService;
  private final InvoiceInterface<Invoice> coinPaymentService;
  private final InvoiceInterface<CryptoClouldCreate> cryptocloudService;

  @Autowired
  public InvoiceController(
      @Qualifier("nowPaymentService") InvoiceInterface<Invoice> nowPaymentService,
      @Qualifier("coinPaymentService") InvoiceInterface<Invoice> coinPaymentService,
      @Qualifier("cryptocloudService") InvoiceInterface<CryptoClouldCreate> cryptocloudService) {
    this.nowPaymentService = nowPaymentService;
    this.coinPaymentService = coinPaymentService;
    this.cryptocloudService = cryptocloudService;
  }

  @Operation(summary = "Create NowPayment invoice", description = "Creates a new invoice using NowPayment service")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Invoice created successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid input"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @PostMapping("/now-payment")
  public ResponseEntity<Invoice> createNowPaymentInvoice(@Valid @RequestBody Invoice invoice) {
    return ResponseEntity.ok(nowPaymentService.createInvoice(invoice));
  }

  @Operation(summary = "Create CoinPayment invoice", description = "Creates a new invoice using CoinPayment service")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Invoice created successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid input"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @PostMapping("/coin-payment")
  public ResponseEntity<Invoice> createCoinPaymentInvoice(@Valid @RequestBody Invoice invoice) {
    return ResponseEntity.ok(coinPaymentService.createInvoice(invoice));
  }

  @Operation(summary = "Handle CoinPayment IPN", description = "Handles IPN (Instant Payment Notification) callbacks from CoinPayment.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "IPN received and processed successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid IPN data"),
      @ApiResponse(responseCode = "403", description = "Invalid HMAC signature"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @PostMapping("/coinpayment/ipn")
  public ResponseEntity<String> handleCoinPaymentIPN(HttpServletRequest request,
      @RequestParam MultiValueMap<String, String> params,
      @RequestBody(required = false) String body) {

    /**
     * CoinPayment IPN body sample:
     * Body sample:
     * amount1=0.001&amount2=0.001&buyer_name=CoinPayments+API&currency1=LTC&currency2=LTC&email=vietkhanh1310%40gmail.com&
     * fee=1.0E-5&ipn_id=f9af837618459442f1362f1693b155e2&ipn_mode=hmac&ipn_type=api&ipn_version=1.0&item_name=NowPayment+test+invoice&item_number=88e93b69-04a0-4c25-bab3-6570d6778de9&merchant=56fda0962d12bc5d076dfd93949bb343&net=0.00099&received_amount=0.001&received_confirms=3&status=100&status_text=Complete&txn_id=CPJF09SNB7FSZRECUGUZWPN5HP
     */

    // convert body to map
    Map<String, String> bodyMap = new HashMap<>();
    String[] bodyParts = body.split("&");
    for (String part : bodyParts) {
      String[] keyValue = part.split("=");
      bodyMap.put(keyValue[0], keyValue[1]);
    }
    logger.info("Body map: {}", bodyMap);

    coinPaymentService.verifyInvoice(bodyMap);

    return ResponseEntity.ok("IPN received");
  }

  @GetMapping("/coinpayment/transaction/{txid}")
  public ResponseEntity<String> getTxInfo(@PathVariable String txid) {
    try {
      String result = coinPaymentService.getTransactionInfo(txid);
      return ResponseEntity.ok(result);
    } catch (Exception e) {
      logger.error("Error getting transaction info: {}", e.getStackTrace(), e);
      return ResponseEntity.status(HttpStatus.SC_INTERNAL_SERVER_ERROR)
          .body("Error: " + e.getMessage());
    }
  }

  @PostMapping("/cryptocloud")
  public ResponseEntity<Invoice> createCryptoCloudInvoice(@Valid @RequestBody CryptoClouldCreate invoice) {
    return ResponseEntity.ok(cryptocloudService.createInvoice(invoice));
  }

  @PostMapping("/cryptocloud/notify")
  public ResponseEntity<String> notify(
      @RequestParam(value = "invoice_id", required = false) String invoiceId,
      @RequestParam(value = "status", required = false) String status,
      @RequestParam(value = "currency", required = false) String currency,
      @RequestParam(value = "order_id", required = false) String orderId,
      @RequestParam(value = "token", required = false) String token,
      @RequestBody(required = false) String body,
      HttpServletRequest request) {

    logger.info("CryptoCloud notify received - Content-Type: {}", request.getContentType());

    Map<String, String> bodyMap = new HashMap<>();

    // Handle form-encoded data (primary case)
    if (invoiceId != null) {
      bodyMap.put("invoice_id", invoiceId);
      bodyMap.put("status", status);
      bodyMap.put("currency", currency);
      bodyMap.put("order_id", orderId);
      bodyMap.put("token", token);
      logger.info("Form data - Invoice ID: {}, Status: {}, Currency: {}, Order ID: {}, Token: {}",
          invoiceId, status, currency, orderId, token);
    }
    // Handle raw body data (fallback for form-encoded as body)
    else if (body != null && !body.isEmpty()) {
      String[] bodyParts = body.split("&");
      for (String part : bodyParts) {
        String[] keyValue = part.split("=", 2);
        if (keyValue.length == 2) {
          bodyMap.put(keyValue[0], keyValue[1]);
        }
      }
      logger.info("Body data parsed: {}", bodyMap);
    }

    if (bodyMap.isEmpty()) {
      logger.error("No valid notification data received");
      return ResponseEntity.badRequest().body("No valid notification data");
    }

    cryptocloudService.verifyInvoice(bodyMap);
    return ResponseEntity.ok("OK");
  }
}
