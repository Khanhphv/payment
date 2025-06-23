package payment_gateways.payment.controller;

import payment_gateways.payment.model.ApiResponse;
import payment_gateways.payment.model.License;
import payment_gateways.payment.model.Web3PaymentRequest;
import payment_gateways.payment.service.InvoiceService;
import payment_gateways.payment.service.Web3Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

  private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

  private final InvoiceService invoiceService;

  @Autowired
  public PaymentController(InvoiceService invoiceService) {
    this.invoiceService = invoiceService;
  }

  @PostMapping("/web3")
  public ResponseEntity<ApiResponse<License>> createWeb3Payment(@RequestBody Web3PaymentRequest request) {
    try {
      License response = invoiceService.createLicenseKey(request);
      return ResponseEntity.ok(ApiResponse.success(response));
    } catch (Exception e) {
      logger.error("Error creating web3 payment: {}", e.getMessage());
      return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
    }
  }
}