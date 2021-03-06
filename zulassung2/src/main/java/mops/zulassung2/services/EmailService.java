package mops.zulassung2.services;

import mops.zulassung2.model.dataobjects.Student;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;

@Service
public class EmailService {


  private final JavaMailSender emailSender;

  @Value("${email_body_text}")
  private String emailBodyText = "Beim Erstellen der Nachricht ist ein Fehler aufgetreten.";
  @Value("${warning_email_body_text}")
  private String warningEmailBodyText = "Beim Erstellen der Nachricht ist ein Fehler aufgetreten.";
  @Value("${receipt_storage_duration}")
  private String receiptStorageDuration = "";


  public EmailService(JavaMailSender emailSender) {
    this.emailSender = emailSender;
  }

  private static boolean isValidEmailAddress(String email) {
    EmailValidator validator = EmailValidator.getInstance();
    return validator.isValid(email);
  }

  private void sendMessage(String receiver, String subject, String text, File attach, String filename)
      throws MessagingException {
    MimeMessage message = emailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message, true);
    if (!isValidEmailAddress(receiver)) {
      throw new MessagingException();
    }
    helper.setTo(receiver);
    helper.setSubject(subject);
    helper.setText(text);
    helper.addAttachment(filename, attach);
    emailSender.send(message);
  }

  /**
   * Sends an E-mail with the information.
   *
   * @param student        Gives an Student into the method.
   * @param currentSubject Gives the Subject of the Student into the method.
   */
  public void sendMail(Student student, String currentSubject, File file)
      throws MessagingException {
    String emailText = createCustomizedEmailBodyText(student, currentSubject);
    String mail = student.getEmail();
    String subject = "Ihr Zulassungsnachweis zum Fach: ";
    sendMessage(mail, subject + currentSubject, emailText, file, file.getName());
  }

  private String createCustomizedEmailBodyText(Student student, String currentSubject) {
    String customizedEmailBodyText = emailBodyText;
    customizedEmailBodyText = customizedEmailBodyText.replace(":name", student.getName());
    customizedEmailBodyText = customizedEmailBodyText.replace(":modul", currentSubject);
    customizedEmailBodyText = customizedEmailBodyText.replace(":duration", receiptStorageDuration);
    customizedEmailBodyText = customizedEmailBodyText.replace(":break", "\n");
    return customizedEmailBodyText;
  }

  /**
   * Sends an email without attachment.
   *
   * @param receiver receiver of the mail
   * @param subject  subject of the mail
   * @param text     Is setting up the Text for the mail
   */
  public void sendSimpleMessage(String receiver, String subject, String text)
      throws MessagingException {
    MimeMessage message = emailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message, true);
    if (!isValidEmailAddress(receiver)) {
      throw new MessagingException();
    }
    helper.setTo(receiver);
    helper.setSubject(subject);
    helper.setText(text);
    emailSender.send(message);
  }

  /**
   * Sends an E-Mail with the Information.
   *
   * @param student        Is setting the Student´s name.
   * @param currentSubject Is setting the Student´s subject.
   */
  public void sendWarningMail(Student student, String currentSubject, String currentDeadLine)
      throws MessagingException {
    String emailText = createWarningEmailBodyText(student, currentSubject, currentDeadLine);
    String mail = student.getEmail();
    String subject = "Ihre fehlende Zulassung zur Klausur im Fach: ";
    sendSimpleMessage(mail, subject + currentSubject, emailText);
  }

  private String createWarningEmailBodyText(Student student, String currentSubject, String currentDeadLine) {
    String customizedWarningEmailBodyText = warningEmailBodyText;
    customizedWarningEmailBodyText = customizedWarningEmailBodyText.replace(":name", student.getName());
    customizedWarningEmailBodyText = customizedWarningEmailBodyText.replace(":modul", currentSubject);
    customizedWarningEmailBodyText = customizedWarningEmailBodyText.replace(":abgabefrist", currentDeadLine);
    customizedWarningEmailBodyText = customizedWarningEmailBodyText.replace(":break", "\n");
    return customizedWarningEmailBodyText;
  }

}