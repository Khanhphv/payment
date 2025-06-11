package payment_gateways.payment.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import payment_gateways.payment.service.HealthCheckService;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthCheckController {

  private final HealthCheckService healthCheckService;

  @Autowired
  public HealthCheckController(HealthCheckService healthCheckService) {
    this.healthCheckService = healthCheckService;
  }

  @GetMapping("/health")
  public ResponseEntity<Map<String, Object>> checkHealth() {
    Map<String, Object> response = new HashMap<>();
    boolean mongoStatus = healthCheckService.checkMongoDBConnection();

    response.put("status", mongoStatus ? "UP" : "DOWN");
    response.put("mongodb", mongoStatus ? "connected" : "disconnected");
    response.put("timestamp", System.currentTimeMillis());

    return ResponseEntity
        .status(mongoStatus ? 200 : 503)
        .body(response);
  }
}