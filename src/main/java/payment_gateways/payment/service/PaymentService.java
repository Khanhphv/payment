package payment_gateways.payment.service;

import payment_gateways.payment.model.Payment;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class PaymentService {
  private final List<Payment> payments = new ArrayList<>();
  private final AtomicLong idGenerator = new AtomicLong(1);

  public Payment createPayment(Payment payment) {
    payment.setId(idGenerator.getAndIncrement());
    payment.setCreatedAt(java.time.LocalDateTime.now());
    payment.setStatus("PENDING");
    payments.add(payment);
    return payment;
  }

  public List<Payment> getAllPayments() {
    return new ArrayList<>(payments);
  }

  public Payment getPaymentById(Long id) {
    return payments.stream()
        .filter(payment -> payment.getId().equals(id))
        .findFirst()
        .orElse(null);
  }

  public Payment updatePayment(Long id, Payment updatedPayment) {
    return payments.stream()
        .filter(payment -> payment.getId().equals(id))
        .findFirst()
        .map(payment -> {
          payment.setAmount(updatedPayment.getAmount());
          payment.setCurrency(updatedPayment.getCurrency());
          payment.setDescription(updatedPayment.getDescription());
          payment.setPaymentMethod(updatedPayment.getPaymentMethod());
          payment.setStatus(updatedPayment.getStatus());
          return payment;
        })
        .orElse(null);
  }

  public boolean deletePayment(Long id) {
    return payments.removeIf(payment -> payment.getId().equals(id));
  }
}