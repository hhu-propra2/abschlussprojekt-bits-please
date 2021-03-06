package mops.zulassung2.model.fileparsing;

import mops.zulassung2.model.dataobjects.Student;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FileParser {

  private ValidatorInterface validatorInterface;
  private CSVLineParserInterface csvLineParserInterface;

  public FileParser(ValidatorInterface validatorInterface) {
    this.validatorInterface = validatorInterface;
    this.csvLineParserInterface = new CSVLineParser();
  }

  public FileParser(ValidatorInterface validatorInterface, CSVLineParserInterface csvLineParserInterface) {
    this.validatorInterface = validatorInterface;
    this.csvLineParserInterface = csvLineParserInterface;
  }

  /**
   * Diese Methode wird vom OrganisatorController aufgerufen, nachdem eine File hochgeladen wurde.
   * Das File wird eingelesen und die enthaltenden Studenten zunächst erzeugt
   * und anschließend in einer ArrayList gespeichert.
   * *
   *
   * @return gibt eine ArrayList zurück, welche die erzeugten Studenten enthält.
   */
  public List<Student> processCSV(MultipartFile file) {
    List<Student> studentList = new ArrayList<>();
    File studentFile = saveFile(file);
    Reader reader = null;
    CSVParser csvParser = null;

    try {
      reader = Files.newBufferedReader(studentFile.toPath());
      csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withHeader());
    } catch (IOException e) {
      e.printStackTrace();
    }

    boolean csvIsValid = validatorInterface.validateCSV(csvParser);

    if (!csvIsValid) {
      deleteFile(studentFile);
      return null;
    }

    for (CSVRecord csvRecord : csvParser) {
      Student currentStudent = csvLineParserInterface.parseLine(csvRecord);
      studentList.add(currentStudent);
    }

    if (studentList.size() <= 0) {
      deleteFile(studentFile);
      return null;
    }

    deleteFile(studentFile);

    return studentList;
  }

  /**
   * This method is called by the OrganisatorService.
   * The content of the file is saved line by line.
   *
   * @return ArrayList, which contains the lines of the receipt
   */
  public List<String> processTXT(MultipartFile file) {
    try {
      File receipt = saveFile(file);
      String receiptContent = Files.readString(receipt.toPath(), StandardCharsets.UTF_8);
      List<String> lines = receiptContent.lines().collect(Collectors.toList());

      boolean txtIsValid = validatorInterface.validateTXT(lines);

      if (!txtIsValid) {
        deleteFile(receipt);
        return null;
      }

      Files.deleteIfExists(receipt.toPath());
      return lines;

    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  private File saveFile(MultipartFile file) {
    File convFile = null;
    try {
      String fileName = file.getOriginalFilename();
      convFile = new File(fileName);
      FileOutputStream fos = new FileOutputStream(convFile);
      fos.write(file.getBytes());
      fos.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return convFile;
  }

  private void deleteFile(File file) {
    try {
      Files.deleteIfExists(file.toPath());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
