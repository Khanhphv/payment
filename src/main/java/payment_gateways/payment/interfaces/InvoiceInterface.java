package payment_gateways.payment.interfaces;

import payment_gateways.payment.model.Invoice;
import java.util.Map;

public interface InvoiceInterface<P> {

  Invoice createInvoice(P payload);

  String getTransactionInfo(String id) throws Exception;

  void verifyInvoice(Map<String, String> bodyMap);
}
