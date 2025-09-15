package payment_gateways.payment.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import payment_gateways.payment.model.Invoice;
import payment_gateways.payment.repository.InvoiceRepository;
import payment_gateways.payment.service.CryptoCloudService;
import payment_gateways.payment.service.EmailService;

@RestController
@RequestMapping("/api")
public class HealthCheckController {

  private static final Logger logger = LoggerFactory.getLogger(HealthCheckController.class);

  @Autowired
  private EmailService emailService;

  @Autowired
  private InvoiceRepository invoiceRepository;

  @Autowired
  private CryptoCloudService cryptoCloudService;

  @Autowired
  public HealthCheckController() {
  }

  @GetMapping("/health")
  public ResponseEntity<String> checkHealth() {
    return ResponseEntity.ok("ok");
  }

  @GetMapping("/test-email")
  public ResponseEntity<String> testSendMail(@RequestParam String to) {
    Invoice invoice = invoiceRepository.findAll().get(0);
    try {
      emailService.sendInvoiceEmail(invoice);
    } catch (Exception e) {
      logger.error("{}", e.getStackTrace());
      return ResponseEntity.internalServerError().body("Error sending email: " + e.getMessage());
    }
    return ResponseEntity.ok("Test email sent to " + to);
  }

  @GetMapping("/resend-email")
  public ResponseEntity<String> resetEmail(@RequestParam String to, @RequestParam String invoiceNumber) {
    Invoice invoice = invoiceRepository.findByInvoiceNumber(invoiceNumber).get();
    Map<String, String> bodyMap = new HashMap<>();
    bodyMap.put("invoice_id", invoice.getInvoiceNumber());
    bodyMap.put("status", "success");
    bodyMap.put("currency", invoice.getCurrency());
    cryptoCloudService.verifyInvoice(bodyMap);
    return ResponseEntity.ok("Email resend to " + to);
  }
}