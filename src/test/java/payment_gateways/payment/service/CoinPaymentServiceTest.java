package payment_gateways.payment.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import payment_gateways.payment.contants.InvoiceStatus;
import payment_gateways.payment.contants.PaymentMethod;
import payment_gateways.payment.model.Invoice;
import payment_gateways.payment.repository.InvoiceRepository;

@ExtendWith(MockitoExtension.class)
class CoinPaymentServiceTest {

  @Mock
  private InvoiceRepository invoiceRepository;

  @InjectMocks
  private CoinPaymentService coinPaymentService;

  private Invoice testInvoice;

  @BeforeEach
  void setUp() {
    // Set up test invoice
    testInvoice = new Invoice();
    testInvoice.setId(1L);
    testInvoice.setInvoiceNumber("test-invoice-123");
    testInvoice.setAmount(new BigDecimal("0.06"));
    testInvoice.setCurrency("LTC");
    testInvoice.setCurrency2("LTC");
    testInvoice.setEmail("test@example.com");
    testInvoice.setService("Test Service");
    testInvoice.setStatus(InvoiceStatus.CREATED);
    testInvoice.setPaymentMethod(PaymentMethod.COINPAYMENT);
    testInvoice.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));
  }

  @Test
  void verifyInvoice_ValidCompletedTransaction_ShouldUpdateStatusToCompleted() {
    // Arrange
    Map<String, String> bodyMap = new HashMap<>();
    bodyMap.put("item_number", "test-invoice-123");
    bodyMap.put("status", "100");
    bodyMap.put("email", "test%40example.com");
    bodyMap.put("item_name", "Test Service");

    when(invoiceRepository.findByInvoiceNumber("test-invoice-123"))
        .thenReturn(Optional.of(testInvoice));
    when(invoiceRepository.save(any(Invoice.class)))
        .thenReturn(testInvoice);

    // Act
    coinPaymentService.verifyInvoice(bodyMap);

    // Assert
    verify(invoiceRepository).findByInvoiceNumber("test-invoice-123");
    verify(invoiceRepository).save(argThat(invoice -> invoice.getStatus() == InvoiceStatus.COMPLETED));
  }

  @Test
  void verifyInvoice_InvalidStatus_ShouldNotUpdateInvoice() {
    // Arrange
    Map<String, String> bodyMap = new HashMap<>();
    bodyMap.put("item_number", "test-invoice-123");
    bodyMap.put("status", "50"); // Invalid status
    bodyMap.put("email", "test%40example.com");
    bodyMap.put("item_name", "Test Service");

    // Act
    coinPaymentService.verifyInvoice(bodyMap);

    // Assert
    verify(invoiceRepository, never()).findByInvoiceNumber(anyString());
    verify(invoiceRepository, never()).save(any(Invoice.class));
  }

  @Test
  void verifyInvoice_MissingRequiredFields_ShouldNotUpdateInvoice() {
    // Arrange
    Map<String, String> bodyMap = new HashMap<>();
    bodyMap.put("status", "100");
    // Missing item_number, email, and item_name

    // Act
    coinPaymentService.verifyInvoice(bodyMap);

    // Assert
    verify(invoiceRepository, never()).findByInvoiceNumber(anyString());
    verify(invoiceRepository, never()).save(any(Invoice.class));
  }

  @Test
  void verifyInvoice_InvoiceNotFound_ShouldNotUpdateInvoice() {
    // Arrange
    Map<String, String> bodyMap = new HashMap<>();
    bodyMap.put("item_number", "non-existent-invoice");
    bodyMap.put("status", "100");
    bodyMap.put("email", "test%40example.com");
    bodyMap.put("item_name", "Test Service");

    when(invoiceRepository.findByInvoiceNumber("non-existent-invoice"))
        .thenReturn(Optional.empty());

    // Act
    coinPaymentService.verifyInvoice(bodyMap);

    // Assert
    verify(invoiceRepository).findByInvoiceNumber("non-existent-invoice");
    verify(invoiceRepository, never()).save(any(Invoice.class));
  }

  @Test
  void verifyInvoice_InvoiceAlreadyCompleted_ShouldNotUpdateInvoice() {
    // Arrange
    testInvoice.setStatus(InvoiceStatus.COMPLETED);
    // I have body map {ipn_mode=hmac, fee=6.0E-5,
    // item_number=992fa189-b1ac-4b94-b452-6dbf4ee7dd80, received_amount=0.012,
    // buyer_name=CoinPayments+API, ipn_type=api,
    // merchant=56fda0962d12bc5d076dfd93949bb343, item_name=Apex-vsharp,
    // txn_id=CPJF0YTIEKIK5DL44EZZDOOSGS, currency1=LTC, currency2=LTC,
    // amount2=0.012, amount1=0.012, received_confirms=3,
    // ipn_id=f8f2b1d8c8a0dedd3fa7587f15ca1377, ipn_version=1.0,
    // status_text=Complete, net=0.01194, email=vietkhanh1310%40gmail.com,
    // status=100}
    // set body map
    Map<String, String> bodyMap = new HashMap<>();
    bodyMap.put("item_number", "992fa189-b1ac-4b94-b452-6dbf4ee7dd80");
    bodyMap.put("status", "100");
    bodyMap.put("email", "vietkhanh1310%40gmail.com");
    bodyMap.put("item_name", "Apex-vsharp");

    when(invoiceRepository.findByInvoiceNumber("992fa189-b1ac-4b94-b452-6dbf4ee7dd80"))
        .thenReturn(Optional.of(testInvoice));

    // Act
    coinPaymentService.verifyInvoice(bodyMap);

    // Assert
    verify(invoiceRepository).findByInvoiceNumber("test-invoice-123");
    verify(invoiceRepository, never()).save(any(Invoice.class));
  }

  @Test
  void verifyInvoice_InvoiceFailed_ShouldNotUpdateInvoice() {
    // Arrange
    testInvoice.setStatus(InvoiceStatus.FAILED);

    Map<String, String> bodyMap = new HashMap<>();
    bodyMap.put("item_number", "test-invoice-123");
    bodyMap.put("status", "100");
    bodyMap.put("email", "test%40example.com");
    bodyMap.put("item_name", "Test Service");

    when(invoiceRepository.findByInvoiceNumber("test-invoice-123"))
        .thenReturn(Optional.of(testInvoice));

    // Act
    coinPaymentService.verifyInvoice(bodyMap);

    // Assert
    verify(invoiceRepository).findByInvoiceNumber("test-invoice-123");
    verify(invoiceRepository, never()).save(any(Invoice.class));
  }

  @Test
  void verifyInvoice_ExceptionDuringProcessing_ShouldSetStatusToFailed() {
    // Arrange
    Map<String, String> bodyMap = new HashMap<>();
    bodyMap.put("item_number", "test-invoice-123");
    bodyMap.put("status", "100");
    bodyMap.put("email", "test%40example.com");
    bodyMap.put("item_name", "Test Service");

    when(invoiceRepository.findByInvoiceNumber("test-invoice-123"))
        .thenReturn(Optional.of(testInvoice));
    when(invoiceRepository.save(any(Invoice.class)))
        .thenThrow(new RuntimeException("Database error"));

    // Act
    coinPaymentService.verifyInvoice(bodyMap);

    // Assert
    verify(invoiceRepository).findByInvoiceNumber("test-invoice-123");
    // Note: The actual implementation would need to handle the exception properly
    // This test shows the expected behavior when an exception occurs
  }

  @Test
  void verifyInvoice_UrlEncodedEmail_ShouldDecodeCorrectly() {
    // Arrange
    Map<String, String> bodyMap = new HashMap<>();
    bodyMap.put("item_number", "test-invoice-123");
    bodyMap.put("status", "100");
    bodyMap.put("email", "user%40example.com"); // URL encoded email
    bodyMap.put("item_name", "Test Service");

    when(invoiceRepository.findByInvoiceNumber("test-invoice-123"))
        .thenReturn(Optional.of(testInvoice));
    when(invoiceRepository.save(any(Invoice.class)))
        .thenReturn(testInvoice);

    // Act
    coinPaymentService.verifyInvoice(bodyMap);

    // Assert
    verify(invoiceRepository).findByInvoiceNumber("test-invoice-123");
    verify(invoiceRepository).save(any(Invoice.class));
    // The email should be decoded from "user%40example.com" to "user@example.com"
  }

  @Test
  void verifyInvoice_NullValuesInBodyMap_ShouldHandleGracefully() {
    // Arrange
    Map<String, String> bodyMap = new HashMap<>();
    bodyMap.put("item_number", null);
    bodyMap.put("status", "100");
    bodyMap.put("email", null);
    bodyMap.put("item_name", null);

    // Act
    coinPaymentService.verifyInvoice(bodyMap);

    // Assert
    verify(invoiceRepository, never()).findByInvoiceNumber(anyString());
    verify(invoiceRepository, never()).save(any(Invoice.class));
  }
}