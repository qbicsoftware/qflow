package life.qbic;

import submitter.parameters.IntParameter;
import submitter.parameters.Parameter;
import submitter.parameters.StringParameter;

public class ParameterFactory {

  public static Parameter getParameter(Object value) {
    if (value instanceof Integer) {
      Parameter param = new IntParameter("title", "description", false, true, -1, 500);
      param.setValue(value);
      return param;
    } else {
      Parameter param = new StringParameter("title", "description", false, false, null);
      param.setValue((String) value);
      return param;
    }
  }

}
