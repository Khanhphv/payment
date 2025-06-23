package payment_gateways.payment.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import payment_gateways.payment.contants.InvoiceStatus;
import payment_gateways.payment.contants.PaymentMethod;
import payment_gateways.payment.model.Invoice;
import payment_gateways.payment.model.License;
import payment_gateways.payment.model.Web3PaymentRequest;
import payment_gateways.payment.repository.InvoiceRepository;

@Service
public class InvoiceService {

  private static final Logger logger = LoggerFactory.getLogger(InvoiceService.class);

  @Autowired
  private InvoiceRepository invoiceRepository;

  @Autowired
  private Web3Service web3Service;

  public License createLicenseKey(Web3PaymentRequest request) {
    String txHash = request.getTxHash();
    String network = request.getNetwork();
    Invoice invoice = findOrCreateInvoice(request);
    License license = null;
    try {
      // Get transaction status from blockchain
      Web3Service.TransactionInfo transactionInfo = web3Service.getTransactionStatus(txHash, network);
      if (transactionInfo.getStatus() == Web3Service.TransactionStatus.CONFIRMED) {
        KeyService keyService = new KeyService();
        license = keyService.getLicenseByService(request.getService());
        invoice.setStatus(InvoiceStatus.COMPLETED);
        updateInvoice(invoice);
      }
      return license;

    } catch (Exception e) {
      logger.error("Failed to generate license key, transaction request: {}", request);
      invoice.setStatus(InvoiceStatus.FAILED);
      invoice.setDescription("Failed to generate license key");
      updateInvoice(invoice);
      throw new RuntimeException(e.getMessage());
    }
  }

  public Invoice findOrCreateInvoice(Web3PaymentRequest request) {
    try {
      String invoiceNumber = request.getTxHash();
      Optional<Invoice> existingInvoice = invoiceRepository.findByInvoiceNumber(invoiceNumber);
      if (existingInvoice.isEmpty()) {
        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(invoiceNumber);
        invoice.setStatus(InvoiceStatus.CREATED);
        invoice.setCreatedAt(LocalDateTime.now());
        invoice.setAmount(request.getAmount());
        invoice.setCurrency(request.getNetwork());
        invoice.setCurrency2(request.getNetwork());
        invoice.setPaymentMethod(PaymentMethod.WEB3);
        invoice.setEmail("test@test.com");
        invoice.setService(request.getService());

        invoiceRepository.save(invoice);
        return invoice;
      }
      if (existingInvoice.get().getStatus() == InvoiceStatus.COMPLETED) {
        throw new RuntimeException("Invoice already completed");
      }
      return existingInvoice.get();

    } catch (RuntimeException e) {
      throw new RuntimeException(e.getMessage());
    } catch (Exception e) {
      throw new RuntimeException("Web3PaymentRequest: failed to create invoice: " + e.getMessage());
    }
  }

  public void updateInvoice(Invoice invoice) {
    invoiceRepository.save(invoice);
  }
}
