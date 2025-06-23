package payment_gateways.payment.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.core.methods.response.EthTransaction;
import org.web3j.protocol.core.methods.response.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;

@Service
public class Web3Service {

  private static final Logger logger = LoggerFactory.getLogger(Web3Service.class);

  @Value("${web3.ethereum.rpc.url:https://mainnet.infura.io/v3/YOUR_PROJECT_ID}")
  private String ethereumRpcUrl;

  @Value("${web3.polygon.rpc.url:https://polygon-rpc.com}")
  private String polygonRpcUrl;

  @Value("${web3.bsc.rpc.url:https://bsc-dataseed.binance.org}")
  private String bscRpcUrl;

  @Value("${web3.bsc.testnet.rpc.url:https://data-seed-prebsc-1-s1.binance.org:8545/}")
  private String bscTestnetRpcUrl;

  public enum TransactionStatus {
    PENDING,
    CONFIRMED,
    FAILED,
    NOT_FOUND
  }

  public static class TransactionInfo {
    private TransactionStatus status;
    private String txHash;
    private BigInteger blockNumber;
    private String from;
    private String to;
    private BigInteger value;
    private String gasUsed;
    private String error;

    public TransactionInfo(TransactionStatus status, String txHash) {
      this.status = status;
      this.txHash = txHash;
    }

    // Getters and setters
    public TransactionStatus getStatus() {
      return status;
    }

    public void setStatus(TransactionStatus status) {
      this.status = status;
    }

    public String getTxHash() {
      return txHash;
    }

    public void setTxHash(String txHash) {
      this.txHash = txHash;
    }

    public BigInteger getBlockNumber() {
      return blockNumber;
    }

    public void setBlockNumber(BigInteger blockNumber) {
      this.blockNumber = blockNumber;
    }

    public String getFrom() {
      return from;
    }

    public void setFrom(String from) {
      this.from = from;
    }

    public String getTo() {
      return to;
    }

    public void setTo(String to) {
      this.to = to;
    }

    public BigInteger getValue() {
      return value;
    }

    public void setValue(BigInteger value) {
      this.value = value;
    }

    public String getGasUsed() {
      return gasUsed;
    }

    public void setGasUsed(String gasUsed) {
      this.gasUsed = gasUsed;
    }

    public String getError() {
      return error;
    }

    public void setError(String error) {
      this.error = error;
    }
  }

  public TransactionInfo getTransactionStatus(String txHash, String network) {
    try {
      logger.info("Checking transaction status for txHash: {} on network: {}", txHash, network);

      Web3j web3j = getWeb3jInstance(network);
      String rpcUrl = getRpcUrl(network);
      logger.info("Using RPC URL: {}", rpcUrl);

      // Get transaction details
      EthTransaction transactionResponse = web3j.ethGetTransactionByHash(txHash)
          .send();

      if (transactionResponse.hasError()) {
        logger.error("Error getting transaction: {}", transactionResponse.getError().getMessage());
        return new TransactionInfo(TransactionStatus.NOT_FOUND, txHash);
      }

      if (transactionResponse.getTransaction().isEmpty()) {
        logger.warn("Transaction not found: {}", txHash);
        return new TransactionInfo(TransactionStatus.NOT_FOUND, txHash);
      }

      Transaction transaction = transactionResponse.getTransaction().get();
      if (transaction == null) {
        logger.warn("Transaction is null: {}", txHash);
        return new TransactionInfo(TransactionStatus.NOT_FOUND, txHash);
      }

      logger.info("Transaction found - From: {}, To: {}, Value: {}",
          transaction.getFrom(), transaction.getTo(), transaction.getValue());

      // Get transaction receipt
      EthGetTransactionReceipt receiptResponse = web3j.ethGetTransactionReceipt(txHash)
          .send();

      TransactionInfo info = new TransactionInfo(TransactionStatus.PENDING, txHash);
      info.setFrom(transaction.getFrom());
      info.setTo(transaction.getTo());
      info.setValue(transaction.getValue());

      if (receiptResponse.hasError()) {
        logger.error("Error getting receipt: {}", receiptResponse.getError().getMessage());
        return info; // Still pending
      }

      if (receiptResponse.getTransactionReceipt().isEmpty()) {
        logger.info("Transaction receipt not found yet - still pending: {}", txHash);
        return info; // Still pending
      }

      TransactionReceipt receipt = receiptResponse.getTransactionReceipt().get();
      info.setBlockNumber(receipt.getBlockNumber());
      info.setGasUsed(receipt.getGasUsed().toString());

      logger.info("Transaction receipt found - Block: {}, Gas Used: {}, Status OK: {}",
          receipt.getBlockNumber(), receipt.getGasUsed(), receipt.isStatusOK());

      info.setStatus(TransactionStatus.PENDING);

      // Check if transaction was successful
      if (receipt.isStatusOK()) {
        info.setStatus(TransactionStatus.CONFIRMED);
        logger.info("Transaction confirmed: {}", txHash);
      } else {
        info.setStatus(TransactionStatus.FAILED);
        info.setError("Transaction failed");
        logger.warn("Transaction failed: {}", txHash);
      }

      return info;

    } catch (Exception e) {
      logger.error("Exception checking transaction status: {}", e.getMessage(), e);
      TransactionInfo info = new TransactionInfo(TransactionStatus.NOT_FOUND, txHash);
      info.setError("Error checking transaction: " + e.getMessage());
      return info;
    }
  }

  public CompletableFuture<TransactionInfo> getTransactionStatusAsync(String txHash, String network) {
    return CompletableFuture.supplyAsync(() -> getTransactionStatus(txHash, network));
  }

  private Web3j getWeb3jInstance(String network) {
    String rpcUrl = getRpcUrl(network);
    return Web3j.build(new HttpService(rpcUrl));
  }

  private String getRpcUrl(String network) {
    switch (network.toLowerCase()) {
      case "polygon":
      case "matic":
        return polygonRpcUrl;
      case "bsc":
      case "binance":
        return bscRpcUrl;
      case "bsc-testnet":
      case "binance-testnet":
      case "bsc_testnet":
        return bscTestnetRpcUrl;
      case "ethereum":
      case "eth":
      default:
        return ethereumRpcUrl;
    }
  }
}