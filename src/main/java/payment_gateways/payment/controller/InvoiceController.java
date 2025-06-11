package payment_gateways.payment.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import payment_gateways.payment.interfaces.InvoiceInterface;
import payment_gateways.payment.model.Invoice;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/invoices")
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

  @PostMapping("/now-payment")
  public ResponseEntity<Invoice> createNowPaymentInvoice(@Valid @RequestBody Invoice invoice) {
    return ResponseEntity.ok(nowPaymentService.createInvoice(invoice));
  }

  @PostMapping("/coin-payment")
  public ResponseEntity<Invoice> createCoinPaymentInvoice(@Valid @RequestBody Invoice invoice) {
    return ResponseEntity.ok(coinPaymentService.createInvoice(invoice));
  }
}
