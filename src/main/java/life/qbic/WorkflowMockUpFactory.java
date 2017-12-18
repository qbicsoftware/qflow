package life.qbic;

import de.uni_tuebingen.qbic.beans.DatasetBean;
import de.uni_tuebingen.qbic.beans.WorkflowDescriptionBean;

public class WorkflowMockUpFactory {

  public static WorkflowParameterComponent getWorkflowParameterComponent(
      WorkflowDescriptionBean wdb, DatasetBean dataset) {
    switch (wdb.getName()) {
      case "peptideID":
        return new QcMLWorkflow(dataset);
      default:
        throw new IllegalArgumentException("Workflow: " + wdb.getName() + " not known.");
    }
  }

}
