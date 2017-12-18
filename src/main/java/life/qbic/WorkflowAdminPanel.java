package life.qbic;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.json.JSONException;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import guse.workflowrepresentation.GuseWorkflowRepresentation;
import submitter.Node;
import submitter.Workflow;
import submitter.parameters.Parameter;

public class WorkflowAdminPanel extends CustomComponent {

  /**
   * 
   */
  private static final long serialVersionUID = -149404521653167290L;

  private NativeSelect nativeSelect;
  private NativeSelect selectExperimentType;
  private NativeSelect selectSampleType;
  private NativeSelect selectgridAction;
  private Grid parameterGrid;

  private ParameterComponent preview;
  private HorizontalLayout paramsAndPreview = new HorizontalLayout();
  private TextField version;
  private TextArea description;
  private TextField name;

  private Button gridaction;

  private LinkedHashMap<String, GuseWorkflowRepresentation> workflows;
  private GuseWorkflowRepresentation workflow;
  private WorkflowAdminPanelController wapController;

  private TextField configName;



  WorkflowAdminPanel(Set<Workflow> wfs, WorkflowAdminPanelController wap,
      List<String> experimentTypes, List<String> sampleTypes) {

    this.wapController = wap;


    nativeSelect = new NativeSelect("Workflows");
    selectExperimentType = new NativeSelect("Select Experiment Type", experimentTypes);
    selectSampleType = new NativeSelect("Select Sample Type", sampleTypes);

    version = new TextField("Version");
    description = new TextArea("Description");
    name = new TextField("Name");
    name.setValue("");
    name.setInputPrompt("Put in a workflow name, that the user will see.");
    name.setDescription("Put in a workflow name, that the user will see.");
    HorizontalLayout descriptionLayout = new HorizontalLayout();
    descriptionLayout.setSpacing(true);
    descriptionLayout.addComponent(name);
    descriptionLayout.addComponent(version);
    descriptionLayout.addComponent(description);

    parameterGrid = new Grid("Parameter Grid");
    parameterGrid.setWidth("100%");

    preview = new ParameterComponent();
    configName = new TextField("Name of Configuration File");
    configName.setVisible(false);
    VerticalLayout mainLayout = new VerticalLayout();
    mainLayout.setMargin(new MarginInfo(false, true, true, true));
    mainLayout.setWidth("100%");
    mainLayout.addComponent(nativeSelect);
    mainLayout.addComponent(descriptionLayout);
    mainLayout.addComponent(selectExperimentType);
    mainLayout.addComponent(selectSampleType);
    mainLayout.setSpacing(true);

    VerticalLayout gridandothers = new VerticalLayout();
    paramsAndPreview.setWidth("100%");


    gridaction = new Button("Apply");


    selectgridAction = new NativeSelect();
    selectgridAction.addItem("Show Preview");
    selectgridAction.addItem("Save Config");

    gridandothers.addComponent(parameterGrid);
    gridandothers.addComponent(selectgridAction);
    gridandothers.addComponent(gridaction);
    gridandothers.setWidth("100%");

    paramsAndPreview.removeAllComponents();
    paramsAndPreview.addComponent(gridandothers);
    VerticalLayout previewLayout = new VerticalLayout();
    previewLayout.addComponent(configName);
    previewLayout.addComponent(preview);
    previewLayout.setSpacing(true);
    paramsAndPreview.addComponent(previewLayout);
    paramsAndPreview.setSpacing(true);
    mainLayout.addComponent(paramsAndPreview);


    this.workflows = new LinkedHashMap<String, GuseWorkflowRepresentation>();
    for (Workflow workflow : wfs) {
      nativeSelect.addItem(workflow.getID());
      GuseWorkflowRepresentation w = (GuseWorkflowRepresentation) workflow;
      this.workflows.put(workflow.getID(), w);
    }


    // Define some columns
    parameterGrid.addColumn("node name", String.class);
    parameterGrid.addColumn("description", String.class);
    parameterGrid.addColumn("name", String.class);
    parameterGrid.addColumn("default", Object.class);
    parameterGrid.addColumn("is advanced", Boolean.class);
    parameterGrid.addColumn("is required", Boolean.class);
    // parameterGrid.setFrozenColumnCount(2);
    parameterGrid.setEditorEnabled(false);

    this.setCompositionRoot(mainLayout);


    /*
     * 
     * Listeners
     */

    gridaction.addClickListener(new ClickListener() {


      /**
       * 
       */
      private static final long serialVersionUID = -1876852349797671717L;

      @Override
      public void buttonClick(ClickEvent event) {
        if (selectgridAction.getValue() == null) {
          Notification.show("No Action selected");
          return;
        } else if ((selectSampleType.getValue() == null)
            || (selectExperimentType.getValue() == null)) {
          Notification.show("Please specify associated experiment type and sample type.");

          return;
        }

        switch ((String) selectgridAction.getValue()) {
          case "Show Preview":

            System.out.println("PREVIEW");
            Collection<Object> selectedRows = parameterGrid.getSelectedRows();
            workflow = wapController.showPreview(selectedRows,
                parameterGrid.getContainerDataSource(), workflows.get(nativeSelect.getValue()),
                name.getValue(), description.getValue(), version.getValue(),
                selectExperimentType.getValue().toString(), selectSampleType.getValue().toString());
            preview.buildLayout(workflow);
            preview.setCaption("Preview");
            configName.setVisible(true);
            configName.setValue("");
            break;
          case "Save Config":
            System.out.println("SAVE");

            if (configName.getValue().isEmpty()) {
              Notification.show("please specify a name for the new config file");
              return;
            }
            preview.writeSetParameters();
            preview.writetInputList();

            try {
              wapController.writeWorkflowConfiguration(workflow, configName.getValue());
              Notification.show("Configuration saved!", Type.TRAY_NOTIFICATION);
            } catch (JSONException | IOException e) {
              e.printStackTrace();
              Notification.show("Internal Error: Configuration could not be saved!",
                  Type.TRAY_NOTIFICATION);
            }
            break;
          default:
            Notification.show("No valid action selected.");
        }
      }
    });

    nativeSelect.addValueChangeListener(new ValueChangeListener() {

      /**
       * 
       */
      private static final long serialVersionUID = -3971113931313751310L;

      @Override
      public void valueChange(ValueChangeEvent event) {
        String current = (String) event.getProperty().getValue();
        if (workflows.containsKey(current)) {
          GuseWorkflowRepresentation workflow = workflows.get(current);
          name.setValue("");
          name.setInputPrompt("Put in a workflow name, that the user will see.");
          version.setValue(workflow.getVersion());
          description.setValue(workflow.getDescription());
          updateGrid(workflow);
        } else {
          resetGrid();
          version.setValue("");
          description.setValue("");
        }
      }
    });
  }

  void resetGrid() {
    parameterGrid.setSelectionMode(SelectionMode.NONE);
    parameterGrid.getContainerDataSource().removeAllItems();

  }


  /**
   * if parameter workflow is null, the grid will be reset.
   * 
   * @param workflow
   */
  void updateGrid(GuseWorkflowRepresentation workflow) {
    resetGrid();
    if (workflow != null) {
      for (Node node : workflow.getNodes()) {

        String name = node.getTitle();
        String description = node.getDescription();
        for (String param : node.getParamNames()) {
          Parameter parameter = node.getParam(param);
          String paramname = param;
          Object defaultvalue = parameter.getValue();
          boolean isadvanced = parameter.isAdvanced();
          boolean isrequired = parameter.isRequired();
          Object[] wo = new Object[6];
          wo[0] = name;
          wo[1] = description;
          wo[2] = paramname;
          wo[3] = defaultvalue;
          wo[4] = isadvanced;
          wo[5] = isrequired;
          parameterGrid.addRow(wo);
        }
      }
    }
    parameterGrid.setSelectionMode(SelectionMode.MULTI);
  }
}
