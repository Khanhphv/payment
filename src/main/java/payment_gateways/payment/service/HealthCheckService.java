package payment_gateways.payment.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.mongodb.client.MongoClient;
import org.bson.Document;

@Service
public class HealthCheckService {

  private final MongoClient mongoClient;

  @Autowired
  public HealthCheckService(MongoClient mongoClient) {
    this.mongoClient = mongoClient;
  }

  public boolean checkMongoDBConnection() {
    try {
      Document doc = mongoClient.getDatabase("invoices").getCollection("transactions").find().first();
      System.out.println(doc.toJson());

      return true;
    } catch (Exception e) {
      return false;
    }
  }
}