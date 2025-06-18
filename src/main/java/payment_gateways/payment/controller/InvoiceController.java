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
import org.springframework.util.MultiValueMap;

import java.util.Enumeration;
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
}
