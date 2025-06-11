package payment_gateways.payment.interfaces;

import payment_gateways.payment.model.Invoice;

public interface InvoiceInterface {

  Invoice createInvoice(Invoice invoice);

}
