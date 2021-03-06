package mops.zulassung2.controller;

import mops.zulassung2.model.dataobjects.AccountCreator;
import mops.zulassung2.model.dataobjects.Student;
import mops.zulassung2.model.dataobjects.UploadCSVForm;
import mops.zulassung2.services.EmailService;
import mops.zulassung2.services.FileService;
import mops.zulassung2.services.MinIoService;
import org.apache.commons.io.FilenameUtils;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.annotation.SessionScope;

import javax.mail.MessagingException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@SessionScope
@RequestMapping("/zulassung2")
@Controller
public class UploadApprovedStudentsController {

  private final FileService fileService;
  private final EmailService emailService;
  private final MinIoService minIoService;
  private List<Student> students = new ArrayList<>();
  private UploadCSVForm uploadCSVForm = new UploadCSVForm();
  private AccountCreator accountCreator;
  private String dangerMessage;
  private String warningMessage;
  private String successMessage;


  /**
   * Constructs OrganisatorController by injecting Beans of
   * OrganisatorService, SignatureService and Emailservice.
   *
   * @param fileService  Service for parsing files
   * @param emailService Service for sending emails
   */
  public UploadApprovedStudentsController(FileService fileService,
                                          EmailService emailService,
                                          MinIoService minIoService) {
    accountCreator = new AccountCreator();
    this.fileService = fileService;
    this.emailService = emailService;
    this.minIoService = minIoService;
  }


  /**
   * This method is called for a GET request to /orga/upload-csv.
   *
   * @param token contains account data
   * @param model Spring object that is used as a container to supply the variables
   * @return Returns view orga-upload-csv
   */
  @GetMapping("/upload-approved-students")
  @Secured("ROLE_orga")
  public String orga(KeycloakAuthenticationToken token, Model model) {
    resetMessages();
    model.addAttribute("account", accountCreator.createFromPrincipal(token));
    model.addAttribute("students", students);
    model.addAttribute("form", uploadCSVForm);

    return "upload-approved-students";
  }


  /**
   * This method is called for a POST request to /upload-approved-students.
   *
   * @param form form to be injected
   * @return Redirects to view orga-upload-csv
   */

  @PostMapping("/upload-approved-students")
  @Secured("ROLE_orga")
  public String submit(@ModelAttribute("form") UploadCSVForm form) {
    if (!FilenameUtils.isExtension(form.getMultipartFile().getOriginalFilename(), "csv")) {
      setDangerMessage("Die Datei muss im .csv Format sein!");
      return "redirect:/zulassung2/upload-approved-students";
    }
    uploadCSVForm.setSubject(form.getSubject().replaceAll("[: ]", "-"));
    uploadCSVForm.setSemester(form.getSemester().replaceAll("[: ]", "-"));

    students = fileService.processCSVUpload(form.getMultipartFile());
    if (students == null) {
      setDangerMessage("Die Datei konnte nicht gelesen werden!");
      return "redirect:/zulassung2/upload-approved-students";
    }

    return "redirect:/zulassung2/upload-approved-students";
  }

  /**
   * This method is called for a POST request to /orga/sendmail.
   * It calls "createFilesAndMails" in the EmailService to create emails and then send them.
   *
   * @return Redirects to view upload-approved-students
   */
  @PostMapping("/sendmail")
  @Secured("ROLE_orga")
  public String sendMail() {
    boolean noErrorsOcurredWhileSendingMessages = true;
    boolean noErrorsOcurredWhileSavingReceipts = true;
    for (Student student : students) {
      File file = fileService.createFileFromUI(student, uploadCSVForm.getSubject(), uploadCSVForm.getSemester());
      try {
        emailService.sendMail(student, uploadCSVForm.getSubject(), file);
        fileService.storeReceipt(student, file);
        if (!minIoService.test(student, uploadCSVForm.getSubject())) {
          noErrorsOcurredWhileSavingReceipts = false;
        }
      } catch (MessagingException e) {
        createDangerMessageMultipleStudents(noErrorsOcurredWhileSendingMessages, student);
        noErrorsOcurredWhileSendingMessages = false;
      }
      try {
        Files.deleteIfExists(file.toPath());
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    if (noErrorsOcurredWhileSendingMessages) {
      setSuccessMessage("Alle Emails wurden erfolgreich versendet.");
      setDangerMessageForSavingMinIO(noErrorsOcurredWhileSavingReceipts);
    } else {
      setWarningMessage("Es wurden nicht alle Emails korrekt versendet.");
      setDangerMessageForSavingMinIO(noErrorsOcurredWhileSavingReceipts);
    }
    return "redirect:/zulassung2/upload-approved-students";
  }

  /**
   * This method is called for a POST request to /orga/sendmail/individual.
   * It calls "createFilesAndMails" in the EmailService to create emails and then send them.
   * In doing so, it uses the provided counter to get to the student from the list of students.
   *
   * @return Redirects to view upload-approved-students
   */
  @PostMapping("/sendmail/individual")
  @Secured("ROLE_orga")
  public String sendMail(@RequestParam("count") int count) {
    Student selectedStudent = students.get(count);
    File file = fileService.createFileFromUI(selectedStudent, uploadCSVForm.getSubject(), uploadCSVForm.getSemester());
    try {
      emailService.sendMail(selectedStudent, uploadCSVForm.getSubject(), file);
      fileService.storeReceipt(selectedStudent, file);
      setDangerMessageForMinIOsingle(selectedStudent);
      createSuccessMethodSingleStudent(selectedStudent);
    } catch (MessagingException e) {
      createDangerMethodSingleStudent(selectedStudent);
    }
    try {
      Files.deleteIfExists(file.toPath());
    } catch (IOException e) {
      e.printStackTrace();
    }
    return "redirect:/zulassung2/upload-approved-students";
  }

  private void createDangerMessageMultipleStudents(boolean noErrorsOcurredWhileSendingMessages, Student student) {
    // noErrorsOcurredWhileSendingMessages is only true when this method is called for the first time
    if (noErrorsOcurredWhileSendingMessages) {
      setDangerMessage("An folgende Studenten konnte keine Email versendet werden: "
          + student.getForeName() + " " + student.getName());
    } else {
      setDangerMessage(dangerMessage.concat(", "
          + student.getForeName() + " " + student.getName()));
    }
  }

  private void createSuccessMethodSingleStudent(Student selectedStudent) {
    if (successMessage == null) {
      setSuccessMessage("Email an " + selectedStudent.getForeName() + " "
          + selectedStudent.getName()
          + " wurde erfolgreich versendet.");
    } else {
      setSuccessMessage(successMessage.concat(" Email an " + selectedStudent.getForeName() + " "
          + selectedStudent.getName()
          + " wurde erfolgreich versendet."));
    }
  }

  private void createDangerMethodSingleStudent(Student selectedStudent) {
    if (dangerMessage == null) {
      setDangerMessage("Email an " + selectedStudent.getForeName()
          + " " + selectedStudent.getName()
          + " konnte nicht versendet werden!");
    } else {
      setDangerMessage(dangerMessage.concat(" Email an " + selectedStudent.getForeName()
          + " " + selectedStudent.getName()
          + " konnte nicht versendet werden!"));
    }
  }

  private void setDangerMessageForSavingMinIO(boolean noErrorsOcurredWhileSavingReceipts) {
    if (noErrorsOcurredWhileSavingReceipts && successMessage == null) {
      setWarningMessage(warningMessage.concat(" Quittungen, bei denen der Mailversand erfolgreich war,"
          + " wurden erfolgreich gespeichert."));
    } else if (noErrorsOcurredWhileSavingReceipts) {
      setSuccessMessage(successMessage.concat(" Alle Quittungen"
          + " wurden erfolgreich gespeichert."));
    } else if (dangerMessage == null) {
      setDangerMessage("Die Nachweise konnten nicht gespeichert werden."
          + " Möglicherweise ist MinIO nicht verfügbar.");
    } else {
      setDangerMessage("Die Nachweise konnten nicht gespeichert werden."
          + " Möglicherweise ist MinIO nicht verfügbar. --- " + dangerMessage);
    }
  }

  private void setDangerMessageForMinIOsingle(Student selectedStudent) {
    if (!minIoService.test(selectedStudent, uploadCSVForm.getSubject())) {
      setDangerMessage("Zulassung von "
          + selectedStudent.getForeName()
          + " " + selectedStudent.getName()
          + " zur Veranstaltung "
          + uploadCSVForm.getSubject()
          + " konnte nicht gespeichert werden."
          + " Möglicherweise ist MinIO nicht verfügbar.");
    } else {
      setSuccessMessage("Zulassung von "
          + selectedStudent.getForeName()
          + " " + selectedStudent.getName()
          + " zur Veranstaltung "
          + uploadCSVForm.getSubject()
          + " wurde erfolgreich gespeichert.");
    }
  }

  /**
   * Set Danger Message for the frontend.
   *
   * @param dangerMessage Describe danger
   */
  private void setDangerMessage(String dangerMessage) {
    this.dangerMessage = dangerMessage;
  }

  /**
   * Set Warning Message for the frontend.
   *
   * @param warningMessage Describe warning
   */
  private void setWarningMessage(String warningMessage) {
    this.warningMessage = warningMessage;
  }

  /**
   * Set Success Message for the frontend.
   *
   * @param successMessage Send a joyful message to the user
   */
  private void setSuccessMessage(String successMessage) {
    this.successMessage = successMessage;
  }

  /**
   * Reset UI Messages.
   */
  private void resetMessages() {
    this.dangerMessage = null;
    this.warningMessage = null;
    this.successMessage = null;
  }

  @ModelAttribute("danger")
  String getDanger() {
    return dangerMessage;
  }

  @ModelAttribute("warning")
  String getWarning() {
    return warningMessage;
  }

  @ModelAttribute("success")
  String getSuccess() {
    return successMessage;
  }
}