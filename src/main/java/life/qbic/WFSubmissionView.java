package life.qbic;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.remoting.RemoteAccessException;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.GeneratedPropertyContainer;
import com.vaadin.event.SelectionEvent;
import com.vaadin.event.SelectionEvent.SelectionListener;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinService;
import com.vaadin.server.WebBrowser;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.VerticalLayout;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project;
import de.uni_tuebingen.qbic.beans.DatasetBean;
import logging.Log4j2Logger;
import submitter.SubmitFailedException;
import submitter.Submitter;
import submitter.Workflow;

public class WFSubmissionView extends VerticalLayout implements View {

  /**
   * 
   */
  private static final long serialVersionUID = 6195481125075601620L;

  private logging.Logger LOGGER = new Log4j2Logger(WFSubmissionView.class);
  private VerticalLayout viewContent = new VerticalLayout();

  private ComboBox selectSpaces;
  private ComboBox selectProjects;

  // private Grid availableDatasets = new Grid("Available Datasets");
  private Grid availableWorkflows = new Grid();

  private Button submitWorkflow = new Button("Submit");
  private Button resetParameters = new Button("Reset Parameters");

  private ParameterComponent parameterComponent = new ParameterComponent();
  private InputFilesComponent inputFileComponent = new InputFilesComponent();

  BeanItemContainer<DatasetBean> projectDatasets;
  private Submitter submitter;

  private VerticalLayout submission;

  private VerticalLayout workflows;

  private String user;

  private OpenbisControl openControl;


  public WFSubmissionView(Submitter submitter, OpenbisControl openControl, String user,
      int browserHeight, int browserWidth, WebBrowser browser) {
    this.openControl = openControl;
    this.user = user;
    this.submitter = submitter;
    this.selectSpaces = new ComboBox("Select Space", openControl.getOpenbis().listSpaces());
    this.buildLayout(browserHeight, browserWidth, browser);
    this.addComponentListeners();
  }

  private void buildLayout(int browserHeight, int browserWidth, WebBrowser browser) {
    // clean up first
    viewContent.removeAllComponents();
    viewContent.setWidth("100%");
    viewContent.setMargin(true);

    viewContent.setWidth((browserWidth * 0.6f), Unit.PIXELS);

    // select openBis data
    VerticalLayout data = new VerticalLayout();
    HorizontalLayout selectData = new HorizontalLayout();

    selectProjects = new ComboBox("Select Project");
    selectData.addComponent(selectSpaces);
    selectData.addComponent(selectProjects);
    selectData.setSpacing(true);
    data.setMargin(new MarginInfo(false, true, true, true));

    data.addComponent(selectData);
    // data.addComponent(availableDatasets);
    // availableDatasets.setVisible(false);
    // availableDatasets.setSizeFull();
    // availableDatasets.setWidth("100%");

    data.setCaption("Data");
    data.setIcon(FontAwesome.DATABASE);
    data.setSpacing(true);

    // select available workflows
    workflows = new VerticalLayout();
    VerticalLayout workflowsContent = new VerticalLayout();
    workflows.setMargin(new MarginInfo(false, true, true, true));

    workflowsContent.addComponent(availableWorkflows);
    // availableWorkflows.setWidth("100%");
    workflows.setVisible(false);

    workflows.setCaption("Available Workflows");
    workflows.setIcon(FontAwesome.EXCHANGE);
    workflows.addComponent(workflowsContent);
    workflows.setWidth(100.0f, Unit.PERCENTAGE);

    // submission
    submission = new VerticalLayout();
    VerticalLayout submissionContent = new VerticalLayout();
    HorizontalLayout buttonContent = new HorizontalLayout();
    submission.setMargin(new MarginInfo(false, true, true, true));

    availableWorkflows.setSizeFull();
    submissionContent.setSpacing(true);
    submissionContent.addComponent(inputFileComponent);
    submissionContent.addComponent(parameterComponent);
    submissionContent.addComponent(buttonContent);

    buttonContent.addComponent(resetParameters);
    buttonContent.addComponent(submitWorkflow);

    submission.setCaption("Submission");
    submission.setIcon(FontAwesome.PLAY);
    submission.addComponent(submissionContent);
    submission.setWidth(100.0f, Unit.PERCENTAGE);
    submission.setVisible(false);

    // add sections to layout
    viewContent.addComponent(data);
    viewContent.addComponent(workflows);
    viewContent.addComponent(submission);

    this.addComponent(viewContent);
  }

  public void rebuildLayout(int height, int width, WebBrowser browser) {
    this.buildLayout(height, width, browser);
  }

  private void addComponentListeners() {

    /*
     * this.availableDatasets.addSelectionListener(new SelectionListener() { private static final
     * long serialVersionUID = 4033655694590766143L;
     * 
     * @Override public void select(SelectionEvent event) { Object selectedDataset =
     * availableDatasets.getSelectedRow(); submission.setVisible(false);
     * workflows.setVisible(false); //Object selectedDataset =
     * datasetContainer.getItem(availableDatasets.getSelectedRow()); if (selectedDataset == null)
     * return; if (selectedDataset instanceof DatasetBean) { DatasetBean dataset = (DatasetBean)
     * selectedDataset; updateWorkflowSelection(dataset); } else { Notification.
     * show("Selected item is not a valid dataset. Please contact your project manager." ,
     * Type.ERROR_MESSAGE); } } } );
     */
    this.availableWorkflows.addSelectionListener(new SelectionListener() {

      /**
       * 
       */
      private static final long serialVersionUID = 2628561841420694483L;

      @Override
      public void select(SelectionEvent event) {

        // TODO get path of datasetBean and set it as input ?!
        Workflow selectedWorkflow = (Workflow) availableWorkflows.getSelectedRow();

        if (selectedWorkflow != null) {
          // DatasetBean datasetBean = (DatasetBean) availableDatasets.getSelectedRow();

          // TODO internal setting of dataset(s) to input file ports? do not show to user
          // selectedWorkflow.setPathOfInput(datasetBean.getFullPath());

          updateParameterView(selectedWorkflow, projectDatasets);

          resetParameters.setVisible(true);
          submission.setVisible(true);
        }
      }
    });

    this.resetParameters.addClickListener(new ClickListener() {
      private static final long serialVersionUID = 1L;

      @Override
      public void buttonClick(ClickEvent event) {
        // TODO reset InputList
        parameterComponent.resetParameters();
      }
    });

    this.submitWorkflow.addClickListener(new ClickListener() {
      private static final long serialVersionUID = 1L;

      @Override
      public void buttonClick(ClickEvent event) {
        List<DatasetBean> selectedDatasets = inputFileComponent.writetInputList();
        Workflow submittedWf = parameterComponent.getWorkflow();
        if (selectedDatasets == null || submittedWf == null) {
          return;
        }
        try {
          String experimentCode = openControl.registerWFExperiment(
              selectSpaces.getValue().toString(), selectProjects.getValue().toString(),
              submittedWf.getExperimentType(), submittedWf.getID(), submittedWf.getVersion(), user);


          List<String> parents = openControl.getConnectedSamples(selectedDatasets);
          String sampleType = submittedWf.getSampleType();
          // if(sampleType == null || sampleType.isEmpty()) sampleType = "Q_WF_MS_PEPTIDEID_RUN";

          String sampleCode = openControl.registerWFSample(selectSpaces.getValue().toString(),
              selectProjects.getValue().toString(), experimentCode, sampleType, parents);

          String openbisId = String.format("%s-%s-%s-%s", selectSpaces.getValue().toString(),
              selectProjects.getValue().toString(), experimentCode, sampleCode);

          LOGGER.info("User: " + user + " is submitting workflow " + submittedWf.getID());
          String submit_id = submitter.submit(submittedWf, openbisId, user);
          LOGGER.info("Workflow has guse id: " + submit_id);

          openControl.setWorkflowID(selectSpaces.getValue().toString(),
              selectProjects.getValue().toString(), experimentCode, submit_id);
          Notification.show("Workflow submitted", Type.TRAY_NOTIFICATION);
        } catch (ConnectException | IllegalArgumentException | SubmitFailedException e) {
          LOGGER.error("Submission failed, probably gUSE. " + e.getMessage(), e.getStackTrace());
          Notification.show(
              "Workflow submission failed due to internal errors! Please try again later or contact your project manager.",
              Type.TRAY_NOTIFICATION);
          try {
            VaadinService.getCurrentResponse().sendError(HttpServletResponse.SC_GATEWAY_TIMEOUT,
                "An error occured, while trying to connect to the database. Please try again later, or contact your project manager.");
          } catch (IOException | IllegalArgumentException e1) {
            // TODO Auto-generated catch block
            VaadinService.getCurrentResponse().setStatus(HttpServletResponse.SC_GATEWAY_TIMEOUT);
          }
        } catch (RemoteAccessException e) {
          LOGGER.error("Submission failed, probably openbis.", e.getStackTrace());
          Notification.show(
              "Workflow submission failed due to internal errors! Please try again later or contact your project manager.",
              Type.TRAY_NOTIFICATION);
        } catch (Exception e) {
          LOGGER.error("Internal error", e.getStackTrace());
          Notification.show(
              "Workflow submission failed due to internal errors! Please try again later or contact your project manager.",
              Type.TRAY_NOTIFICATION);
        }
      }
    });


    selectSpaces.addValueChangeListener(new ValueChangeListener() {
      private static final long serialVersionUID = -7989618240891719302L;

      @Override
      public void valueChange(ValueChangeEvent event) {
        // TODO change that, beans & datahandler

        selectProjects.removeAllItems();
        selectProjects.setEnabled(false);
        // availableDatasets.setVisible(false);
        workflows.setVisible(false);
        submission.setVisible(false);

        String space = (String) selectSpaces.getValue();
        if (space != null) {
          List<String> projects = new ArrayList<String>();
          for (Project p : openControl.getOpenbis().getProjectsOfSpace(space)) {
            projects.add(p.getCode());
          }
          selectProjects.addItems(projects);
          selectProjects.setEnabled(true);
        }
      }
    });


    selectProjects.addValueChangeListener(new ValueChangeListener() {
      private static final long serialVersionUID = 8475566653811278646L;

      @Override
      public void valueChange(ValueChangeEvent event) {
        // TODO change that, beans & datahandler

        // availableDatasets.removeAllColumns();
        // availableDatasets.setVisible(false);
        workflows.setVisible(false);
        submission.setVisible(false);

        String projectID = (String) selectProjects.getValue();
        String spaceId = (String) selectSpaces.getValue();
        if (projectID != null) {
          long startTime = System.nanoTime();

          List<ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet> datasets =
              openControl.getOpenbis().getClientDatasetsOfProjectByIdentifierWithSearchCriteria(
                  "/" + spaceId + "/" + projectID);
          List<String> datasetTypesInProject = new ArrayList<String>();
          long endTime = System.nanoTime();
          System.out.println(
              String.format("getClientDatasetsOfProjectByIdentifierWithSearchCriteria took %f s",
                  ((endTime - startTime) / 1000000000.0)));
          startTime = System.nanoTime();
          projectDatasets = (BeanItemContainer<DatasetBean>) openControl.fillTable2(datasets);
          endTime = System.nanoTime();
          System.out.println(
              String.format("fillTable2 took %f s", ((endTime - startTime) / 1000000000.0)));
          startTime = System.nanoTime();

          for (Iterator<DatasetBean> i = projectDatasets.getItemIds().iterator(); i.hasNext();) {
            DatasetBean dsBean = (DatasetBean) i.next();
            datasetTypesInProject.add(dsBean.getFileType());
          }
          endTime = System.nanoTime();
          System.out
              .println(String.format("for loop took %f s", ((endTime - startTime) / 1000000000.0)));
          startTime = System.nanoTime();


          updateWorkflowSelection(datasetTypesInProject);
          endTime = System.nanoTime();
          System.out.println(String.format("updateWorkflowSelectiontook %f s",
              ((endTime - startTime) / 1000000000.0)));
          startTime = System.nanoTime();
        }
      }
    });
  }

  private void updateParameterView(Workflow workFlow,
      BeanItemContainer<DatasetBean> projectDatasets) {
    this.inputFileComponent.buildLayout(workFlow, projectDatasets);
    this.parameterComponent.buildLayout(workFlow);
  }

  protected void updateWorkflowSelection(DatasetBean dataset) {
    updateSelection(suitableWorkflows(dataset.getFileType()));
  }

  protected void updateWorkflowSelection(List<String> datasetTypes) {
    updateSelection(suitableWorkflows(datasetTypes));
  }

  /**
   * updates availableWorkflows to contain only workflows according to dataset selection.
   * 
   * @param suitableWorkflows
   */
  void updateSelection(BeanItemContainer<Workflow> suitableWorkflows) {
    if (!(suitableWorkflows.size() > 0)) {
      Notification notif =
          new Notification("No suitable workflows found. Pleace contact your project manager.",
              Type.TRAY_NOTIFICATION);

      // Customize it
      notif.setDelayMsec(60000);
      notif.setPosition(Position.MIDDLE_CENTER);

      // Show it in the page
      notif.show(Page.getCurrent());
    }

    availableWorkflows.setContainerDataSource(filtergpcontainer(suitableWorkflows));
    availableWorkflows.setColumnOrder("name", "description", "version", "fileTypes");
    workflows.setVisible(true);
  }


  @Override
  public void enter(ViewChangeEvent event) {
    // TODO Auto-generated method stub

  }

  /**
   * returns all known workflows, that can be executed with the given filetype
   * 
   * @param fileType
   * @return
   */
  BeanItemContainer<Workflow> suitableWorkflows(String fileType) {
    try {
      return submitter.getAvailableSuitableWorkflows(fileType);
    } catch (Exception e) {
      e.printStackTrace();
      return new BeanItemContainer<Workflow>(Workflow.class);
    }
  }

  /**
   * returns all known workflows, that can be executed with one of the given filetypes
   * 
   * @param fileType
   * @return
   */
  BeanItemContainer<Workflow> suitableWorkflows(List<String> fileType) {
    try {
      return submitter.getAvailableSuitableWorkflows(fileType);
    } catch (Exception e) {
      e.printStackTrace();
      return new BeanItemContainer<Workflow>(Workflow.class);
    }
  }


  /**
   * filter columns from grid
   * 
   * @param suitableWorkflows
   * @return
   */
  GeneratedPropertyContainer filtergpcontainer(BeanItemContainer<Workflow> suitableWorkflows) {
    // ONLY SHOW SPECIFIC COLUMNS IN GRID
    GeneratedPropertyContainer gpcontainer = new GeneratedPropertyContainer(suitableWorkflows);

    gpcontainer.removeContainerProperty("ID");
    gpcontainer.removeContainerProperty("data");
    gpcontainer.removeContainerProperty("datasetType");
    gpcontainer.removeContainerProperty("nodes");
    gpcontainer.removeContainerProperty("experimentType");
    gpcontainer.removeContainerProperty("parameterToNodesMapping");
    gpcontainer.removeContainerProperty("parameters");
    gpcontainer.removeContainerProperty("sampleType");
    return gpcontainer;
  }

}
