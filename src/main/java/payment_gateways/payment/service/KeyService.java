package payment_gateways.payment.service;

import org.springframework.stereotype.Service;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import payment_gateways.payment.model.License;

@Service
public class KeyService {
  public String loginToAdmin() {
    String url = "http://193.41.237.5:8080/admin/login";
    RestTemplate restTemplate = new RestTemplate();

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    Map<String, String> body = new HashMap<>();
    body.put("username", "admin");
    body.put("password", "123324@@@");

    HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    try {
      ObjectMapper mapper = new ObjectMapper();
      JsonNode root = mapper.readTree(response.getBody());
      return root.path("token").asText();
    } catch (Exception e) {
      throw new RuntimeException("Failed to parse token from response", e);
    }
  }

  public String generateLicenseWithService(String service) {
    String url = "http://193.41.237.5:8080/admin/licenses/generate-with-service";
    RestTemplate restTemplate = new RestTemplate();
    String token = loginToAdmin();

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("Accept", "application/json, text/plain, */*");
    headers.set("Authorization", token);

    Map<String, Object> body = new HashMap<>();
    body.put("service", service);
    body.put("version", "1.0");
    body.put("expiry_duration", 1);
    body.put("price", 0);
    body.put("count", 1);
    body.put("notes", "");
    body.put("user_id", 1);

    HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    try {
      return response.getBody();
    } catch (Exception e) {
      throw new RuntimeException("Failed to parse license response", e);
    }
  }

  public License getLicenseByService(String service) {
    String url = "http://193.41.237.5:8080/admin/licenses/generate-with-service";
    RestTemplate restTemplate = new RestTemplate();
    String token = loginToAdmin();

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("Accept", "application/json, text/plain, */*");
    headers.set("Authorization", token);

    Map<String, Object> body = new HashMap<>();
    body.put("service", service);
    body.put("version", "1.0");
    body.put("expiry_duration", 1);
    body.put("price", 0);
    body.put("count", 1);
    body.put("notes", "");
    body.put("user_id", 1);

    HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
    ResponseEntity<License> response = restTemplate.postForEntity(url, request, License.class);
    License license = response.getBody();
    if (license.isSuccess()) {
      return license;
    } else {
      throw new RuntimeException("Failed to generate license key from service: ");
    }

  }
}