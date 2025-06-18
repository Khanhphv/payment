package payment_gateways.payment.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import payment_gateways.payment.interfaces.InvoiceInterface;

import java.time.LocalDateTime;

@Component
public class ScheduledTasks {
  @Scheduled(fixedRate = 30000)
  public void runEvery30Seconds() {
    System.out.println("Job executed at " + LocalDateTime.now());
    InvoiceInterface coinPaymentService = new CoinPaymentService();

    // Add your job logic here
  }
}