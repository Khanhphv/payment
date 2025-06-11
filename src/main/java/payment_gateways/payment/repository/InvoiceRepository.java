package payment_gateways.payment.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import payment_gateways.payment.model.Invoice;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends MongoRepository<Invoice, String> {
  Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

  List<Invoice> findByEmail(String email);

  List<Invoice> findByStatus(payment_gateways.payment.contants.InvoiceStatus status);
}