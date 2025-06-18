package payment_gateways.payment.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import payment_gateways.payment.interfaces.InvoiceInterface;
import payment_gateways.payment.model.Invoice;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/invoices")
@Tag(name = "Invoice", description = "Invoice management APIs")
public class InvoiceController {

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
}
