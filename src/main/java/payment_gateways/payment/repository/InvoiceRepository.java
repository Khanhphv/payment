package payment_gateways.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import payment_gateways.payment.model.Invoice;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
  Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

  List<Invoice> findByEmail(String email);

  List<Invoice> findByStatus(payment_gateways.payment.contants.InvoiceStatus status);

  Optional<Invoice> findByPaymentUrl(String paymentUrl);

}