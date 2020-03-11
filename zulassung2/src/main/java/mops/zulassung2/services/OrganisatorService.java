package mops.zulassung2.services;

import mops.zulassung2.model.Student;
import mops.zulassung2.model.fileparsing.CustomCSVLineParser;
import mops.zulassung2.model.fileparsing.CustomValidator;
import mops.zulassung2.model.fileparsing.FileParser;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrganisatorService {

  public List<Student> processCSVUpload(MultipartFile file) {
    FileParser csvParser = new FileParser(new CustomValidator(), new CustomCSVLineParser());
    List<Student> students = csvParser.processCSV(file);
    if (students == null) {
      students = new ArrayList<>();
    }

    return students;
  }
}
