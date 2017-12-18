package life.qbic;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.vaadin.data.validator.AbstractStringValidator;

public class FileNameValidator extends AbstractStringValidator {


  public FileNameValidator(String errorMessage) {
    super(errorMessage);
    // TODO Auto-generated constructor stub
  }

  @Override
  protected boolean isValidValue(String value) {

    if (value == null) {
      return false;
    } else {
      Pattern pattern = Pattern.compile("[~#@*+%{}<>\\[\\]|\"\\^]");
      Matcher matcher = pattern.matcher(value);
      return !(value.contains(" ") | matcher.find());
    }
  }

}
