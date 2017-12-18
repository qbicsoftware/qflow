package life.qbic;

import com.vaadin.ui.CustomComponent;

import de.uni_tuebingen.qbic.beans.DatasetBean;
import submitter.Workflow;
import submitter.parameters.ParameterSet;

/**
 * Abstract class that will show workflow paramters. It extends the vaadin class CustomComponent. A
 * class that extends this class can be included as a workflow parameter component into the workflow
 * submission layout.
 * 
 * @author wojnar
 * 
 */
public abstract class WorkflowParameterComponent extends CustomComponent {
  static public String datasetType = "noDataSetType";
  private DatasetBean datasetbean;

  public WorkflowParameterComponent(DatasetBean bean) {
    this.datasetbean = bean;
  }

  public WorkflowParameterComponent() {
    // TODO Auto-generated constructor stub
  }

  abstract Workflow getWorkflow();

  abstract ParameterSet getParameters();

  /**
   * resets all paramters of this component to default values
   */
  abstract public void resetParameters();

  /**
   * 
   */
  abstract public void buildLayout();

  abstract public void buildLayout(Workflow wf);

}
