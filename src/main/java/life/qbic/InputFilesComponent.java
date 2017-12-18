package life.qbic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.GeneratedPropertyContainer;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.Grid.SingleSelectionModel;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.TextField;
import com.vaadin.ui.themes.ValoTheme;

import de.uni_tuebingen.qbic.beans.DatasetBean;
import fasta.FastaBean;
import fasta.FastaDB;
import logging.Log4j2Logger;
import submitter.Workflow;
import submitter.parameters.FileListParameter;
import submitter.parameters.FileParameter;
import submitter.parameters.InputList;
import submitter.parameters.Parameter;
import submitter.parameters.ParameterSet;

public class InputFilesComponent extends WorkflowParameterComponent {


  /**
   * 
   */
  private static final long serialVersionUID = -675703070595329585L;
  private TabSheet inputFileForm = new TabSheet();
  private FieldGroup inputFileFieldGroup;
  private Workflow workFlow;

  private logging.Logger LOGGER = new Log4j2Logger(InputFilesComponent.class);

  public InputFilesComponent(Workflow workFlow) {
    this.workFlow = workFlow;
    this.buildLayout(workFlow);
    setCompositionRoot(inputFileForm);
  }

  public InputFilesComponent() {
    setCompositionRoot(inputFileForm);
  }

  @Override
  Workflow getWorkflow() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  ParameterSet getParameters() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void resetParameters() {
    // TODO Auto-generated method stub
  }


  public void buildLayout(Workflow workFlow, BeanItemContainer<DatasetBean> datasets) {
    this.workFlow = workFlow;
    this.setCaption(String.format("<font color=#FF0000>  Select input file(s) </font>"));
    this.setCaptionAsHtml(true);
    buildForm(workFlow, datasets);
  }

  @Override
  public void buildLayout(Workflow workFlow) {
    this.workFlow = workFlow;
    // buildForm(workFlow);
  }

  public void buildForm(Workflow workflow, BeanItemContainer<DatasetBean> datasets) {

    inputFileForm.setHeight(100.0f, Unit.PERCENTAGE);
    inputFileForm.addStyleName(ValoTheme.TABSHEET_FRAMED);
    inputFileForm.addStyleName(ValoTheme.TABSHEET_PADDED_TABBAR);

    inputFileForm.removeAllComponents();
    inputFileForm.setSizeFull();
    inputFileFieldGroup = new FieldGroup();


    for (Map.Entry<String, Parameter> entry : workFlow.getData().getData().entrySet()) {
      GeneratedPropertyContainer gpcontainer = null;
      Grid newGrid = new Grid(gpcontainer);

      if (entry.getValue() instanceof FileParameter) {
        FileParameter fileParam = (FileParameter) entry.getValue();
        List<String> associatedDataTypes = fileParam.getRange();
        // String associatedDataType = fileParam.getTitle();

        if (associatedDataTypes.contains("fasta")) {
          // if (associatedDataType.toLowerCase().equals("fasta")) {
          BeanItemContainer<FastaBean> subContainer =
              new BeanItemContainer<FastaBean>(FastaBean.class);
          FastaDB db = new FastaDB();
          subContainer.addAll(db.getAll());
          gpcontainer = new GeneratedPropertyContainer(subContainer);
          gpcontainer.removeContainerProperty("path");
        }

        else {
          BeanItemContainer<DatasetBean> subContainer =
              new BeanItemContainer<DatasetBean>(DatasetBean.class);

          for (java.util.Iterator<DatasetBean> i = datasets.getItemIds().iterator(); i.hasNext();) {
            DatasetBean dataset = i.next();

            if (associatedDataTypes.contains(dataset.getFileType().toLowerCase())) {
              // if (associatedDataType.toLowerCase().equals(dataset.getFileType().toLowerCase())) {
              subContainer.addBean(dataset);
            }
          }

          gpcontainer = new GeneratedPropertyContainer(subContainer);
          gpcontainer.removeContainerProperty("fullPath");
          gpcontainer.removeContainerProperty("openbisCode");

        }
        newGrid.setContainerDataSource(gpcontainer);
        newGrid.setSelectionMode(SelectionMode.SINGLE);
      }

      else if (entry.getValue() instanceof FileListParameter) {
        FileListParameter fileParam = (FileListParameter) entry.getValue();
        List<String> associatedDataTypes = fileParam.getRange();

        BeanItemContainer<DatasetBean> subContainer =
            new BeanItemContainer<DatasetBean>(DatasetBean.class);

        for (java.util.Iterator<DatasetBean> i = datasets.getItemIds().iterator(); i.hasNext();) {
          DatasetBean dataset = i.next();

          if (associatedDataTypes.contains(dataset.getFileType().toLowerCase())) {
            subContainer.addBean(dataset);
          }
        }

        gpcontainer = new GeneratedPropertyContainer(subContainer);
        gpcontainer.removeContainerProperty("fullPath");
        gpcontainer.removeContainerProperty("openbisCode");

        newGrid.setContainerDataSource(gpcontainer);
        newGrid.setSelectionMode(SelectionMode.MULTI);
      }

      else {
        Notification.show(String.format("Invalid Inputfile Parameter!", entry.getKey()),
            Type.ERROR_MESSAGE);
      }

      HorizontalLayout layout = new HorizontalLayout();
      layout.setMargin(new MarginInfo(true, true, true, true));
      layout.setSizeFull();

      newGrid.setWidth("100%");
      layout.addComponent(newGrid);

      if (newGrid.getContainerDataSource().size() == 0) {
        Notification.show(
            String.format("No dataset of type %s available in this project!", entry.getKey()),
            Type.WARNING_MESSAGE);
        layout.addComponent(newGrid);
      }

      inputFileForm.addTab(layout, entry.getKey());
    }
  }

  // TODO
  public void resetInputList() {
    Collection<Field<?>> registeredFields = inputFileFieldGroup.getFields();
    InputList inpList = workFlow.getData();

    for (Field<?> field : registeredFields) {
      TextField fieldToReset = (TextField) field;
      fieldToReset.setValue(inpList.getParam(field.getCaption()).getValue().toString());
    }
  }

  public List<DatasetBean> writetInputList() {
    /*
     * java.util.Iterator<Component> componentIterator = inputFileForm.iterator();
     * 
     * while (componentIterator.hasNext()) { HorizontalLayout currentLayout = (HorizontalLayout)
     * componentIterator.next(); Grid currentGrid = (Grid) currentLayout.getComponent(0);
     * 
     * if(currentGrid.getSelectionModel() instanceof SingleSelectionModel) { Object selection =
     * currentGrid.getSelectedRow();
     * 
     * if(selection instanceof FastaBean) {
     * 
     * } else { inpList.getData().get(caption).setValue(filePaths); }
     * 
     * } else { Collection<Object> selection = currentGrid.getSelectedRows(); } }
     */
    java.util.Iterator<Component> i = inputFileForm.getComponentIterator();
    InputList inpList = workFlow.getData();

    List<DatasetBean> selectedDatasets = new ArrayList<DatasetBean>();

    while (i.hasNext()) {
      Tab tab = inputFileForm.getTab(i.next());

      HorizontalLayout current = (HorizontalLayout) tab.getComponent();
      java.util.Iterator<Component> j = current.iterator();
      while (j.hasNext()) {
        Grid currentGrid = (Grid) j.next();

        String caption = tab.getCaption();

        if (currentGrid.getSelectionModel() instanceof SingleSelectionModel) {
          Object selectionSingle = currentGrid.getSelectedRow();
          if (selectionSingle == null) {
            String errorMessage = "Warning: Nothing selected for single input parameter " + caption;
            LOGGER.info(errorMessage);
            Notification.show(errorMessage, Type.TRAY_NOTIFICATION);

            // throw new Exception(errorMessage);
            return null;
          }
          if (selectionSingle instanceof FastaBean) {
            FastaBean selectedBean = (FastaBean) selectionSingle;
            inpList.getData().get(caption).setValue(selectedBean.getPath());
          } else {
            DatasetBean selectedBean = (DatasetBean) selectionSingle;
            selectedDatasets.add(selectedBean);
            inpList.getData().get(caption).setValue(selectedBean.getFullPath());
          }

        } else {
          Collection<Object> selectionMulti = currentGrid.getSelectedRows();
          if (selectionMulti == null || selectionMulti.isEmpty()) {
            String errorMessage = "Warning: Nothing selected for multi input parameter " + caption;
            LOGGER.info(errorMessage);
            Notification.show(errorMessage, Type.TRAY_NOTIFICATION);

            // throw new Exception(errorMessage);
            return null;
            // throw new IllegalArgumentException(errorMessage);
          }
          List<String> selectedPaths = new ArrayList<String>();

          for (Object o : selectionMulti) {
            DatasetBean selectedBean = (DatasetBean) o;
            selectedDatasets.add(selectedBean);
            selectedPaths.add(selectedBean.getFullPath());
          }
          inpList.getData().get(caption).setValue(selectedPaths);
        }
      }
    }
    return selectedDatasets;
  }

  @Override
  public void buildLayout() {
    // TODO Auto-generated method stub

  }
}
