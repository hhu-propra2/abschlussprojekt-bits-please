package mops.zulassung2.model.fileparsing;

import org.apache.commons.csv.CSVParser;

import java.util.List;
import java.util.Map;

public class Validator implements ValidatorInterface {
  @Override
  public boolean validateCSV(CSVParser csvParser) {
    if (csvParser == null) {
      return false;
    }
    Map<String, Integer> headerMap = csvParser.getHeaderMap();
    if (headerMap.size() != 4) {
      return false;
    }

    for (String key : headerMap.keySet()) {
      Integer index = headerMap.get(key);
      if ((index == 0 && !key.equals("matriculationnumber"))
              || (index == 1 && !key.equals("email"))
              || (index == 2 && !key.equals("name"))
              || (index == 3 && !key.equals("forename"))) {
        return false;
      }
    }

    return true;
  }

  @Override
  public boolean validateTXT(List<String> txtContent) {

    if (txtContent.size() != 2) {
      return false;
    }

    String[] dataObjects = txtContent.get(0).split(" ");
    int counter = 0;
    for (String key : dataObjects) {
      if ((counter == 0 && !key.startsWith("matriculationnumber:"))
              || (counter == 1 && !key.startsWith("email:"))
              || (counter == 2 && !key.startsWith("name:"))
              || (counter == 3 && !key.startsWith("forename:"))
              || (counter == 4 && !key.startsWith("module:"))
              || (counter == 5 && !key.startsWith("semester:"))) {
        return false;
      }
      counter++;
    }
    return counter == 6;
  }
}
