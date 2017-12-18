package life.qbic;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.PropertysetItem;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.TextField;

import de.uni_tuebingen.qbic.beans.DatasetBean;
import submitter.Workflow;
import submitter.parameters.Parameter;
import submitter.parameters.ParameterSet;

public class QcMLWorkflow extends WorkflowParameterComponent {

  static public String datasetType = "MZML";
  final String selectedFileDefault = "No file selected";
  final int precursorTolerancePpmDefault = 15;
  final int fragmentTolerancePpmDefault = 25;
  final Map<String, Path> databaseDefault = new HashMap<String, Path>();
  final int retentionTimeToleranceSecDefault = 60;

  String selectedFileCurrent = selectedFileDefault;
  String databaseCurrent;
  PropertysetItem item = new PropertysetItem();
  FieldGroup binder;

  FormLayout fl = new FormLayout();

  @PropertyId("selectedFileCurrent")
  TextField file = new TextField("Executing QC Workflow on file");
  @PropertyId("precursorTolerancePpmCurrent")
  TextField precTol = new TextField("precursor tolerance (ppm)");
  @PropertyId("fragmentTolerancePpmCurrent")
  TextField fracTol = new TextField("fragment tolerance (ppm)");
  @PropertyId("databaseCurrent")
  NativeSelect nativeSelectDatabase = new NativeSelect("database");
  @PropertyId("retentionTimeToleranceSecCurrent")
  TextField retTimeTol = new TextField("retention time tolerance (sec)");


  public QcMLWorkflow() {
    super();
    buildLayout();
    setCompositionRoot(fl);
  }

  public QcMLWorkflow(DatasetBean dataset) {
    super(dataset);
    initFastaOptions();

    selectedFileCurrent = dataset.getFileName();
    buildLayout();
    setCompositionRoot(fl);
  }

  private void initFastaOptions() {

    databaseDefault.put("human", Paths.get("/somewhere/over/the/rainbow"));
    databaseDefault.put("mouse", Paths.get("/tom/but/not/jerry"));
    databaseDefault.put("cat", Paths.get("/i/am/gonna/eat/you/little/fishy"));
    databaseDefault.put("lister", Paths.get("Wait. Are you trying to tell me everybody's dead?"));
    databaseDefault.put("kryten", Paths.get("Good afternoon, Mister David, sir."));
    databaseDefault.put("rimmer", Paths.get(
        "I loved that little lemming. I built him a little wall he could hurl himself off of."));
    databaseDefault.put("holly", Paths.get("Probably not serious, don't panic."));
    databaseDefault.put("kochanski", Paths.get(
        "How did I end up like this, on a ship where the fourth most popular pastime is going down to the laundry room and watching my knickers spin dry?"));
    databaseCurrent = "human";
    nativeSelectDatabase.addItems(databaseDefault.keySet());

  }

  @Override
  public void buildLayout() {
    binder = new FieldGroup(item);
    item.addItemProperty("selectedFileCurrent", new ObjectProperty<String>(selectedFileCurrent));
    resetParameters();

    fl = new FormLayout(file, precTol, fracTol, nativeSelectDatabase, retTimeTol);
  }

  @Override
  public void resetParameters() {
    item.addItemProperty("precursorTolerancePpmCurrent",
        new ObjectProperty<Integer>(precursorTolerancePpmDefault));
    item.addItemProperty("fragmentTolerancePpmCurrent",
        new ObjectProperty<Integer>(fragmentTolerancePpmDefault));
    item.addItemProperty("databaseCurrent", new ObjectProperty<String>(databaseCurrent));
    item.addItemProperty("retentionTimeToleranceSecCurrent",
        new ObjectProperty<Integer>(retentionTimeToleranceSecDefault));
    binder.bindMemberFields(this);
  }

  @Override
  ParameterSet getParameters() {
    Collection<?> collection = item.getItemPropertyIds();
    Iterator<?> it = collection.iterator();
    HashMap<String, Parameter> parameters = new HashMap<String, Parameter>();
    while (it.hasNext()) {
      Object object = it.next();
      Parameter parameter = ParameterFactory.getParameter(item.getItemProperty(object).getValue());
      parameters.put((String) object, parameter);
    }
    ParameterSet parameterSet = new ParameterSet("qc proteomics", "set by user", parameters);
    return parameterSet;
  }

  @Override
  Workflow getWorkflow() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void buildLayout(Workflow wf) {
    // TODO Auto-generated method stub

  }

}
