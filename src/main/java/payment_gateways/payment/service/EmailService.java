package payment_gateways.payment.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import payment_gateways.payment.model.Invoice;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class EmailService {

  @Autowired
  private JavaMailSender mailSender;

  String CC_ADDRESS = "vietkhanh1310@gmail.com";
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm");

  public void sendSimpleEmail(String to, String subject, String text) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo(to);
    message.setSubject(subject);
    message.setText(text);
    message.setCc(CC_ADDRESS);
    mailSender.send(message);
  }

  public void sendInvoiceEmail(Invoice invoice) throws MessagingException, IOException {
    String subject = "Invoice Created - " + invoice.getInvoiceNumber();
    String htmlContent = buildInvoiceEmailTemplate(invoice);
    sendHtmlEmail(invoice.getEmail(), subject, htmlContent, CC_ADDRESS);
  }

  /**
   * Build invoice email template
   */
  private String buildInvoiceEmailTemplate(Invoice invoice) throws IOException {
    // Read template from file
    ClassPathResource resource = new ClassPathResource("templates/invoice-email.html");
    String template = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

    // Format data with null checks
    NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);
    String formattedAmount = invoice.getAmount() != null ? currencyFormatter.format(invoice.getAmount()) : "0.00";
    String formattedDate = invoice.getCreatedAt() != null ? invoice.getCreatedAt().format(DATE_FORMATTER) : "N/A";

    // Build conditional sections
    String descriptionSection = "";
    String descriptionMargin = "0";
    if (invoice.getDescription() != null && !invoice.getDescription().isEmpty()) {
      descriptionSection = String.format(
          "<div style=\"display: flex; justify-content: space-between; margin-bottom: 0; border-top: 1px solid #eee; padding-top: 8px;\">"
              +
              "<span style=\"font-weight: bold;\">Description:</span><span>%s</span></div>",
          invoice.getDescription());
      descriptionMargin = "10px";
    }

    String paymentButton = "";
    if (invoice.getPaymentUrl() != null && !invoice.getPaymentUrl().isEmpty()) {
      paymentButton = String.format(
          "<div style=\"text-align: center; margin: 25px 0;\">" +
              "<a href=\"%s\" style=\"background: #007bff; color: white; padding: 12px 25px; text-decoration: none; border-radius: 5px; font-weight: bold; display: inline-block;\">"
              +
              "Complete Payment</a></div>",
          invoice.getPaymentUrl());
    }

    // Replace placeholders with null checks
    return template
        .replace("{{invoiceNumber}}", invoice.getInvoiceNumber() != null ? invoice.getInvoiceNumber() : "N/A")
        .replace("{{amount}}", formattedAmount)
        .replace("{{currency}}", invoice.getCurrency() != null ? invoice.getCurrency() : "")
        .replace("{{paymentMethod}}",
            invoice.getPaymentMethod() != null ? invoice.getPaymentMethod().toString() : "N/A")
        .replace("{{status}}", invoice.getStatus() != null ? invoice.getStatus().toString() : "UNKNOWN")
        .replace("{{createdDate}}", formattedDate)
        .replace("{{descriptionMargin}}", descriptionMargin)
        .replace("{{descriptionSection}}", descriptionSection)
        .replace("{{paymentButton}}", paymentButton);
  }

  private void sendHtmlEmail(String to, String subject, String htmlContent, String cc) throws MessagingException {
    MimeMessage message = mailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
    helper.setTo(to);
    helper.setSubject(subject);
    helper.setText(htmlContent, true);
    helper.setCc(cc);
    mailSender.send(message);
  }
}