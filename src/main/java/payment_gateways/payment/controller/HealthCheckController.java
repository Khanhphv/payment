package payment_gateways.payment.controller;

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
}