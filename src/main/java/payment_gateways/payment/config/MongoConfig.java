package payment_gateways.payment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;

import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

@Configuration
public class MongoConfig {

  @Value("${spring.data.mongodb.uri}")
  private String mongoUri;

  @Bean
  public MongoClient mongoClient() {
    String connectionString = "mongodb+srv://invoices_mongo:hnZlkobwSvmvq4FN@invoices.tu00wjy.mongodb.net/?retryWrites=true&w=majority&appName=Invoices";
    ServerApi serverApi = ServerApi.builder()
        .version(ServerApiVersion.V1)
        .build();
    MongoClientSettings settings = MongoClientSettings.builder()
        .applyConnectionString(new ConnectionString(connectionString))
        .serverApi(serverApi)
        .build();

    MongoClient mongoClient = MongoClients.create(settings);
    try {
      // Send a ping to confirm a successful connection
      MongoDatabase database = mongoClient.getDatabase("admin");
      database.runCommand(new Document("ping", 1));
      System.out.println("Pinged your deployment. You successfully connected to MongoDB!");
      return mongoClient;
    } catch (MongoException e) {
      e.printStackTrace();
      throw e;
    }
  }

  @Bean
  public MongoTemplate mongoTemplate() {
    return new MongoTemplate(new SimpleMongoClientDatabaseFactory(mongoClient(), "payment_db"));
  }
}