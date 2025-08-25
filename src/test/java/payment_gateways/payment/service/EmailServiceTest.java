package payment_gateways.payment.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

  @Mock
  private JavaMailSender mailSender;

  @InjectMocks
  private EmailService emailService;

  private String testTo;
  private String testSubject;
  private String testText;

  @BeforeEach
  void setUp() {
    testTo = "test@example.com";
    testSubject = "Test Subject";
    testText = "Test email content";
  }

  @Test
  void sendSimpleEmail_ValidParameters_ShouldSendEmailSuccessfully() {
    // Arrange
    ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

    // Act
    emailService.sendSimpleEmail(testTo, testSubject, testText);

    // Assert
    verify(mailSender).send(messageCaptor.capture());

    SimpleMailMessage capturedMessage = messageCaptor.getValue();
    assertNotNull(capturedMessage);

    String[] toAddresses = capturedMessage.getTo();
    assertNotNull(toAddresses);
    assertEquals(testTo, toAddresses[0]);
    assertEquals(testSubject, capturedMessage.getSubject());
    assertEquals(testText, capturedMessage.getText());

    String[] ccAddresses = capturedMessage.getCc();
    assertNotNull(ccAddresses);
    assertEquals("vietkhanh1310@gmail.com", ccAddresses[0]);
  }

  @Test
  void sendSimpleEmail_MultipleRecipients_ShouldSendToAllRecipients() {
    // Arrange
    String multipleRecipients = "test1@example.com,test2@example.com";
    ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

    // Act
    emailService.sendSimpleEmail(multipleRecipients, testSubject, testText);

    // Assert
    verify(mailSender).send(messageCaptor.capture());

    SimpleMailMessage capturedMessage = messageCaptor.getValue();

    String[] toAddresses = capturedMessage.getTo();
    assertNotNull(toAddresses);
    assertEquals(multipleRecipients, toAddresses[0]);
  }

  @Test
  void sendSimpleEmail_EmptySubject_ShouldSendEmailWithEmptySubject() {
    // Arrange
    String emptySubject = "";
    ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

    // Act
    emailService.sendSimpleEmail(testTo, emptySubject, testText);

    // Assert
    verify(mailSender).send(messageCaptor.capture());

    SimpleMailMessage capturedMessage = messageCaptor.getValue();
    assertEquals(emptySubject, capturedMessage.getSubject());
  }

  @Test
  void sendSimpleEmail_EmptyText_ShouldSendEmailWithEmptyText() {
    // Arrange
    String emptyText = "";
    ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

    // Act
    emailService.sendSimpleEmail(testTo, testSubject, emptyText);

    // Assert
    verify(mailSender).send(messageCaptor.capture());

    SimpleMailMessage capturedMessage = messageCaptor.getValue();
    assertEquals(emptyText, capturedMessage.getText());
  }

  @Test
  void sendSimpleEmail_NullTo_ShouldSendEmailWithNullTo() {
    // Arrange
    ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

    // Act
    emailService.sendSimpleEmail(null, testSubject, testText);

    // Assert
    verify(mailSender).send(messageCaptor.capture());

    SimpleMailMessage capturedMessage = messageCaptor.getValue();

    String[] toAddresses = capturedMessage.getTo();
    assertNotNull(toAddresses);
    assertEquals(1, toAddresses.length);
    assertNull(toAddresses[0]); // The array contains one null element
    assertEquals(testSubject, capturedMessage.getSubject());
    assertEquals(testText, capturedMessage.getText());

    String[] ccAddresses = capturedMessage.getCc();
    assertNotNull(ccAddresses);
    assertEquals("vietkhanh1310@gmail.com", ccAddresses[0]);
  }

  @Test
  void sendSimpleEmail_NullSubject_ShouldSendEmailWithNullSubject() {
    // Arrange
    ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

    // Act
    emailService.sendSimpleEmail(testTo, null, testText);

    // Assert
    verify(mailSender).send(messageCaptor.capture());

    SimpleMailMessage capturedMessage = messageCaptor.getValue();
    assertNull(capturedMessage.getSubject());
  }

  @Test
  void sendSimpleEmail_NullText_ShouldSendEmailWithNullText() {
    // Arrange
    ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

    // Act
    emailService.sendSimpleEmail(testTo, testSubject, null);

    // Assert
    verify(mailSender).send(messageCaptor.capture());

    SimpleMailMessage capturedMessage = messageCaptor.getValue();
    assertNull(capturedMessage.getText());
  }

  @Test
  void sendSimpleEmail_MailSenderThrowsException_ShouldPropagateException() {
    // Arrange
    MailException mailException = new MailException("Mail server error") {
    };
    doThrow(mailException).when(mailSender).send(any(SimpleMailMessage.class));

    // Act & Assert
    assertThrows(MailException.class, () -> {
      emailService.sendSimpleEmail(testTo, testSubject, testText);
    });

    verify(mailSender).send(any(SimpleMailMessage.class));
  }

  @Test
  void sendSimpleEmail_LongSubjectAndText_ShouldHandleCorrectly() {
    // Arrange
    String longSubject = "This is a very long subject line that might exceed normal email client limits but should still be handled correctly by the email service";
    String longText = "This is a very long email content that contains multiple paragraphs and should be handled correctly by the email service. "
        .repeat(10);

    ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

    // Act
    emailService.sendSimpleEmail(testTo, longSubject, longText);

    // Assert
    verify(mailSender).send(messageCaptor.capture());

    SimpleMailMessage capturedMessage = messageCaptor.getValue();
    assertEquals(longSubject, capturedMessage.getSubject());
    assertEquals(longText, capturedMessage.getText());
  }

  @Test
  void sendSimpleEmail_SpecialCharactersInContent_ShouldHandleCorrectly() {
    // Arrange
    String specialSubject = "Test with special chars: àáâãäåæçèéêë ñòóôõö ùúûüý";
    String specialText = "Email with special characters: €£¥ and symbols: @#$%^&*()";

    ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

    // Act
    emailService.sendSimpleEmail(testTo, specialSubject, specialText);

    // Assert
    verify(mailSender).send(messageCaptor.capture());

    SimpleMailMessage capturedMessage = messageCaptor.getValue();
    assertEquals(specialSubject, capturedMessage.getSubject());
    assertEquals(specialText, capturedMessage.getText());
  }

  @Test
  void sendSimpleEmail_AlwaysSetsCcToVietKhanh_ShouldVerifyCcAddress() {
    // Arrange
    ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

    // Act
    emailService.sendSimpleEmail(testTo, testSubject, testText);

    // Assert
    verify(mailSender).send(messageCaptor.capture());

    SimpleMailMessage capturedMessage = messageCaptor.getValue();

    String[] ccAddresses = capturedMessage.getCc();
    assertNotNull(ccAddresses);
    assertEquals(1, ccAddresses.length);
    assertEquals("vietkhanh1310@gmail.com", ccAddresses[0]);
  }

  @Test
  void sendSimpleEmail_VerifyMailSenderCalledOnlyOnce_ShouldCallSendOnce() {
    // Act
    emailService.sendSimpleEmail(testTo, testSubject, testText);

    // Assert
    verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
  }
}
