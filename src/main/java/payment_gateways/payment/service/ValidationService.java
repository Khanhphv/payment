package payment_gateways.payment.service;

import org.springframework.stereotype.Service;
import payment_gateways.payment.model.Invoice;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;

@Service
@Validated
public class ValidationService {

  public void validateInvoice(@Valid Invoice invoice) {
    // Additional custom validations can be added here
    validateCurrency(invoice.getCurrency());
  }

  private void validateCurrency(String currency) {
    // Example: Validate against supported currencies
    // if (!isValidCurrency(currency)) {
    // throw new IllegalArgumentException("Unsupported currency: " + currency);
    // }
  }

  private boolean isValidCurrency(String currency) {
    // Add your supported currencies here
    return currency != null && (currency.equals("USD") ||
        currency.equals("EUR") ||
        currency.equals("BTC") ||
        currency.equals("ETH"));
  }
}