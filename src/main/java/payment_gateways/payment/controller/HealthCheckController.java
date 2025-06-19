package payment_gateways.payment.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import payment_gateways.payment.service.EmailService;

@RestController
@RequestMapping("/api")
public class HealthCheckController {

  @Autowired
  private EmailService emailService;

  @Autowired
  public HealthCheckController() {
  }

  @GetMapping("/health")
  public ResponseEntity<String> checkHealth() {
    return ResponseEntity.ok("ok");
  }

  @GetMapping("/test-email")
  public ResponseEntity<String> testSendMail(@RequestParam String to) {
    emailService.sendSimpleEmail(to, "Test Email", "This is a test email from Payment Gateway Service.");
    return ResponseEntity.ok("Test email sent to " + to);
  }
}