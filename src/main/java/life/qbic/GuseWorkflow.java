package life.qbic;

import java.io.File;
import java.util.List;

import submitter.Node;
import submitter.Workflow;
import submitter.parameters.InputList;
import submitter.parameters.ParameterSet;

public class GuseWorkflow extends Workflow {
  enum Type {
    veryBasic
  }

  private File workflowDirectory;

  public GuseWorkflow(String id, String name, String description, String version, InputList il,
      ParameterSet ps, File workflowDirectory, String experimentType, String datasetType) {
    super(id, name, description, version, il, ps, experimentType, datasetType);
    this.workflowDirectory = workflowDirectory;
  }

  public GuseWorkflow(String id, String name, String description, String version, InputList il,
      List<Node> nodes, File workflowDirectory, String experimentType, String datasetType) {
    super(id, name, description, version, il, null, experimentType, datasetType);
    this.nodes = nodes;
    this.workflowDirectory = workflowDirectory;
  }

  public File getWorkflowDir() {
    // TODO Auto-generated method stub
    return workflowDirectory;
  }
}
