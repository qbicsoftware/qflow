package life.qbic;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.vaadin.data.Validator;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.data.util.converter.StringToFloatConverter;
import com.vaadin.data.util.converter.StringToIntegerConverter;
import com.vaadin.data.validator.FloatRangeValidator;
import com.vaadin.data.validator.IntegerRangeValidator;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Field;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.TextField;

import logging.Log4j2Logger;
import submitter.Workflow;
import submitter.parameters.BooleanParameter;
import submitter.parameters.FloatParameter;
import submitter.parameters.InputList;
import submitter.parameters.IntParameter;
import submitter.parameters.Parameter;
import submitter.parameters.ParameterSet;
import submitter.parameters.StringParameter;

public class ParameterComponent extends WorkflowParameterComponent {

  /**
   * 
   */
  private static final long serialVersionUID = -3182823029401916444L;
  private logging.Logger LOGGER = new Log4j2Logger(ParameterComponent.class);

  private FormLayout parameterForm = new FormLayout();
  private FieldGroup parameterFieldGroup;
  private FieldGroup inputListFieldGroup;
  private Workflow workFlow;

  public ParameterComponent(Workflow workFlow) {
    this.workFlow = workFlow;
    this.buildLayout(workFlow);
    setCompositionRoot(parameterForm);
  }

  public ParameterComponent() {
    setCompositionRoot(parameterForm);
  }

  @Override
  public void buildLayout(Workflow workFlow) {
    this.workFlow = workFlow;
    this.setCaption("<font color=#FF0000> Set Parameter Values for Workflow Submission </font>");
    this.setCaptionAsHtml(true);
    buildForm(workFlow);
  }

  public void buildForm(final Workflow workFlow) {

    parameterForm.removeAllComponents();
    parameterFieldGroup = new FieldGroup();
    inputListFieldGroup = new FieldGroup();

    /*
     * for (Map.Entry<String, Parameter> entry : workFlow.getData().getData().entrySet()) {
     * FileParameter param = (FileParameter) entry.getValue(); FileNameValidator fileNameValidator =
     * new FileNameValidator("Please provide a valid file path"); TextField newField =
     * createInputField(param, fileNameValidator);
     * 
     * parameterForm.addComponent(newField); inputListFieldGroup.bind(newField, entry.getKey()); //
     * Have to set it here because field gets cleared upon binding
     * newField.setValue(param.getValue().toString()); }
     */

    for (Map.Entry<String, Parameter> entry : workFlow.getParameters().getParams().entrySet()) {
      if (entry.getValue() instanceof FloatParameter) {
        FloatParameter param = (FloatParameter) entry.getValue();
        FloatRangeValidator floatValidator =
            new FloatRangeValidator(String.format("Parameter has to be in the range of %s to %s",
                param.getMinimum(), param.getMaximum()), param.getMinimum(), param.getMaximum());
        TextField newField =
            createParameterField(param, floatValidator, new StringToFloatConverter());

        parameterForm.addComponent(newField);
        parameterFieldGroup.bind(newField, entry.getKey());
        // Have to set it here because field gets cleared upon binding
        newField.setValue(param.getValue().toString());
      }

      else if (entry.getValue() instanceof IntParameter) {
        IntParameter param = (IntParameter) entry.getValue();
        IntegerRangeValidator intValidator =
            new IntegerRangeValidator(String.format("Parameter has to be in the range of %s to %s",
                param.getMinimum(), param.getMaximum()), param.getMinimum(), param.getMaximum());
        TextField newField =
            createParameterField(param, intValidator, new StringToIntegerConverter());

        parameterForm.addComponent(newField);
        parameterFieldGroup.bind(newField, entry.getKey());
        // Have to set it here because field gets cleared upon binding
        newField.setValue(param.getValue().toString());
      }

      else if (entry.getValue() instanceof StringParameter) {
        StringParameter param = (StringParameter) entry.getValue();
        ComboBox newField = createStringSelectionParameterField(param);

        parameterForm.addComponent(newField);
        parameterFieldGroup.bind(newField, entry.getKey());
        // Have to set it here because field gets cleared upon binding
        newField.setValue(param.getValue().toString());
      }

      else if (entry.getValue() instanceof BooleanParameter) {
        BooleanParameter param = (BooleanParameter) entry.getValue();

        CheckBox newField = createParameterCheckBox(param);

        parameterForm.addComponent(newField);
        parameterFieldGroup.bind(newField, entry.getKey());
        // Have to set it here because field gets cleared upon binding
        newField.setValue((Boolean) param.getValue());
        newField.setRequired(param.isRequired());
      }
    }
  }

  @Override
  Workflow getWorkflow() {
    boolean parametersValid = writeSetParameters();
    if (!parametersValid)
      return null;
    writetInputList();
    return this.workFlow;
  }

  /**
   * writes UI parameters to their model(workflow) equivalent returns true if all fields where set
   * with meaningfull values.
   * 
   * @return
   */
  boolean writeSetParameters() {
    Collection<Field<?>> registeredFields = parameterFieldGroup.getFields();
    ParameterSet paramSet = workFlow.getParameters();

    for (Field<?> field : registeredFields) {
      if (!field.isValid() || field.isEmpty()) {
        String errorMessage = "Warning: Parameter " + field.getCaption() + "is invalid!";
        Notification.show(errorMessage, Type.TRAY_NOTIFICATION);
        LOGGER.info(errorMessage);
        return false;
      }

      Object value = field.getValue();
      paramSet.getParam(field.getCaption()).setValue(value);
    }
    return true;
  }

  void writetInputList() {
    Collection<Field<?>> registeredFields = inputListFieldGroup.getFields();
    InputList inpList = workFlow.getData();
    for (Field<?> field : registeredFields) {
      inpList.getParam(field.getCaption()).setValue(field.getValue().toString());
    }
  }

  @Override
  public void resetParameters() {
    Collection<Field<?>> registeredFields = parameterFieldGroup.getFields();
    ParameterSet paramSet = workFlow.getParameters();

    for (Field field : registeredFields) {
      String resetValue = paramSet.getParam(field.getCaption()).getValue().toString();
      field.setValue(resetValue);
    }
  }

  public void resetInputList() {
    Collection<Field<?>> registeredFields = inputListFieldGroup.getFields();
    InputList inpList = workFlow.getData();

    for (Field<?> field : registeredFields) {
      TextField fieldToReset = (TextField) field;
      fieldToReset.setValue(inpList.getParam(field.getCaption()).getValue().toString());
    }
  }

  private TextField createParameterField(Parameter param, Validator validator,
      Converter converter) {
    TextField field = new TextField(param.getTitle());
    field.setDescription(param.getDescription());
    field.addValidator(validator);
    field.setImmediate(true);
    field.setConverter(converter);
    return field;
  }

  private TextField createInputField(Parameter param, Validator validator) {
    TextField field = new TextField(param.getTitle());
    field.setDescription(param.getDescription());
    field.setWidth("50%");
    field.addValidator(validator);
    field.setImmediate(true);
    return field;
  }

  private CheckBox createParameterCheckBox(Parameter param) {
    String description;
    if (param.getDescription().contains("#br#")) {
      description = param.getDescription().split("#br#")[0];
    } else {
      description = param.getDescription();
    }

    CheckBox box = new CheckBox(param.getTitle());
    box.setDescription(description);
    box.setImmediate(true);
    return box;
  }

  private ComboBox createStringSelectionParameterField(StringParameter param) {
    ComboBox box = new ComboBox(param.getTitle());
    box.setDescription(param.getDescription());
    box.setFilteringMode(FilteringMode.CONTAINS);
    box.addItems(param.getRange());
    // should only be the range.
    box.setNullSelectionAllowed(false);
    box.setImmediate(true);
    return box;
  }

  @Override
  public void buildLayout() {
    // TODO Auto-generated method stub

  }


  @Override
  ParameterSet getParameters() {
    Collection<Field<?>> registeredFields = parameterFieldGroup.getFields();
    ParameterSet paramSet = workFlow.getParameters();

    Map<String, Parameter> updatedParams = new HashMap<String, Parameter>();

    for (Field<?> field : registeredFields) {
      Parameter updatedParam = paramSet.getParam(field.getCaption());
      updatedParam.setValue(field.getValue().toString());
      updatedParams.put(updatedParam.getTitle(), updatedParam);
    }

    ParameterSet updatedParamSet =
        new ParameterSet(workFlow.getName(), workFlow.getDescription(), updatedParams);
    return updatedParamSet;
  }
}
