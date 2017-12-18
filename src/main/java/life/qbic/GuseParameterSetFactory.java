package life.qbic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import submitter.Node;
import submitter.ParameterSetFactory;
import submitter.parameters.FloatParameter;
import submitter.parameters.Parameter;

public class GuseParameterSetFactory extends ParameterSetFactory {

  public static List<Node> getVeryBasicParameterSet(String workflowName, String workflowVersion)
      throws IllegalArgumentException {
    if (workflowName.contains("PeptideID-XTandem") && workflowVersion.equals("321")) {
      return getPeptideID_Xtandem_321();
    } else {
      throw new IllegalArgumentException(
          "workflow " + workflowName + " with version " + workflowVersion + " unknown.");
    }
  }

  public static void writeVeryBasicParameterSet() {

  }

  private static List<Node> getPeptideID_Xtandem_321() {

    FloatParameter fragment_mass_tolerance =
        new FloatParameter("fragment_mass_tolerance", "Fragment mass error", false, true, 0, 500);
    fragment_mass_tolerance.setValue(0.3f);

    FloatParameter precursor_mass_tolerance = new FloatParameter("precursor_mass_tolerance",
        "Precursor mass tolerance", false, true, 0, 500);
    precursor_mass_tolerance.setValue(1.5f);

    Map<String, Parameter> param = new HashMap<String, Parameter>();
    param.put(fragment_mass_tolerance.getTitle(), fragment_mass_tolerance);
    param.put(precursor_mass_tolerance.getTitle(), precursor_mass_tolerance);

    List<Node> nodes = new ArrayList<Node>();

    Node xtandem = new Node("XTandemAdapter", "1", param);
    nodes.add(xtandem);
    return nodes;
  }

}
