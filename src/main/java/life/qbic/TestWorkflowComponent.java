package life.qbic;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.util.converter.StringToFloatConverter;
import com.vaadin.ui.Field;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.TextField;

import submitter.Workflow;
import submitter.parameters.FloatParameter;
import submitter.parameters.InputList;
import submitter.parameters.Parameter;
import submitter.parameters.ParameterSet;

public class TestWorkflowComponent extends WorkflowParameterComponent {

  private FieldGroup fieldGroup;
  private FormLayout form;
  private Workflow workflow;
  private Map<String, Path> databaseDefault;

  public TestWorkflowComponent(Workflow wf, String name) {
    this.workflow = wf;
    buildLayout(wf);
  }

  @Override
  Workflow getWorkflow() {

    Collection<Field<?>> registeredFields = fieldGroup.getFields();

    for (Field<?> field : registeredFields) {
      // TODO set value
      if (field.getCaption().equals("database")) {
        InputList il = workflow.getData();
        il.getParam(0).setValue(databaseDefault.get(field.getValue()).toString());
      } else if (workflow.getNodes().get(0)
          .getParam(field.getCaption()) instanceof FloatParameter) {
        TextField tf = (TextField) field;
        float val = (float) tf.getConvertedValue();
        workflow.getNodes().get(0).getParam(field.getCaption()).setValue(val);
      } else {
        workflow.getNodes().get(0).getParam(field.getCaption()).setValue(field.getValue());
      }
    }

    return this.workflow;
  }

  @Override
  public void resetParameters() {
    Collection<Field<?>> registeredFields = fieldGroup.getFields();

    for (Field<?> field : registeredFields) {
      field.setValue(null);
    }

  }

  @Override
  public void buildLayout(Workflow wf) {
    // TODO remove hard code
    buildFormLayout(wf.getNodes().get(0), wf.getData());
    setCompositionRoot(form);
    // TODO Auto-generated method stub

  }

  private void buildFormLayout(ParameterSet paramSet, InputList inputList) {
    final FieldGroup fieldGroup = new FieldGroup();
    final FormLayout form2 = new FormLayout();

    for (String key : paramSet.getParamNames()) {
      /*
       * if(expInfo.controlledVocabularies.keySet().contains(key)) { ComboBox select = new
       * ComboBox(key); fieldGroup.bind(select,key); form2.addComponent(select);
       * 
       * // Add items with given item IDs for(String item: expInfo.controlledVocabularies.get(key))
       * { select.addItem(item); } select.setValue(expInfo.properties.get(key)); } else {
       */

      TextField tf = new TextField(paramSet.getParam(key).getTitle());
      tf.setValue(paramSet.getParam(key).getValue().toString());

      if (paramSet.getParam(key) instanceof FloatParameter) {
        FloatParameter param = (FloatParameter) paramSet.getParam(key);
        // tf.addValidator(new DoubleRangeValidator("Only float numbers allowed!",
        // param.getMinimum(), param.getMinimum()));
        tf.setConverter(new StringToFloatConverter());
      }

      tf.setDescription(paramSet.getParam(key).getDescription());
      tf.setNullRepresentation("");

      // tf.setValue(paramSet.getParam(key).getTitle());
      fieldGroup.bind(tf, key);
      form2.addComponent(tf);
      // }
    }

    // input lists
    for (int i = 0; i < inputList.size(); i++) {
      Parameter param = inputList.getParam(i);
      String jobName = param.getDescription();
      if (jobName.endsWith("fasta") && param.getTitle().contains("0")) {
        databaseDefault = new HashMap<String, Path>();
        databaseDefault.put("human", Paths.get(String.format("%s\t%s", "FASTA",
            "/lustre_cfc/qbic/WF_testing/inputFiles/uniprot-human.fasta")));
        databaseDefault.put("mouse", Paths.get("/tom/but/not/jerry"));
        databaseDefault.put("cat", Paths.get("/i/am/gonna/eat/you/little/fishy"));
        databaseDefault.put("lister",
            Paths.get("Wait. Are you trying to tell me everybody's dead?"));
        databaseDefault.put("kryten", Paths.get("Good afternoon, Mister David, sir."));
        NativeSelect nativeSelectDatabase = new NativeSelect("database");
        nativeSelectDatabase.addItems(databaseDefault.keySet());
        nativeSelectDatabase.setValue("human");
        fieldGroup.bind(nativeSelectDatabase, param);
        form2.addComponent(nativeSelectDatabase);
      }
    }


    this.fieldGroup = fieldGroup;
    this.form = form2;
  }

  @Override
  ParameterSet getParameters() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void buildLayout() {
    // TODO Auto-generated method stub

  }

}
