package payment_gateways.payment.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import payment_gateways.payment.interfaces.InvoiceInterface;
import payment_gateways.payment.model.Invoice;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.util.MultiValueMap;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/invoices")
@Tag(name = "Invoice", description = "Invoice management APIs")
public class InvoiceController {

  private static final Logger logger = LoggerFactory.getLogger(InvoiceController.class);
  private final InvoiceInterface nowPaymentService;
  private final InvoiceInterface coinPaymentService;

  @Autowired
  public InvoiceController(
      @Qualifier("nowPaymentService") InvoiceInterface nowPaymentService,
      @Qualifier("coinPaymentService") InvoiceInterface coinPaymentService) {
    this.nowPaymentService = nowPaymentService;
    this.coinPaymentService = coinPaymentService;
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
    final String ipnSecret = "YOUR_COINPAYMENTS_IPN_SECRET"; // TODO: Replace with your actual secret

    // // 1. Verify HMAC
    // if (hmacHeader == null || !verifyHmac(ipnSecret, rawBody, hmacHeader)) {
    // return ResponseEntity.status(403).body("Invalid HMAC signature");
    // }

    logger.info("Method: {}", request.getMethod());
    logger.info("URI: {}", request.getRequestURI());
    logger.info("Query: {}", request.getQueryString());
    logger.info("Body: {}", body);

    Enumeration<String> headers = request.getHeaderNames();
    while (headers.hasMoreElements()) {
      String name = headers.nextElement();
      logger.info("Header: {} = {}", name, request.getHeader(name));
    }

    return ResponseEntity.ok("IPN received");
  }

  private boolean verifyHmac(String secret, String body, String hmacHeader) {
    try {
      Mac hmac = Mac.getInstance("HmacSHA512");
      SecretKeySpec key = new SecretKeySpec(secret.getBytes(), "HmacSHA512");
      hmac.init(key);
      byte[] hash = hmac.doFinal(body.getBytes());
      StringBuilder sb = new StringBuilder();
      for (byte b : hash) {
        sb.append(String.format("%02x", b & 0xff));
      }
      String calculatedHmac = sb.toString();
      return calculatedHmac.equalsIgnoreCase(hmacHeader);
    } catch (Exception e) {
      return false;
    }
  }
}
