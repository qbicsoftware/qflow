package life.qbic;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.json.JSONException;

import com.vaadin.data.Container.Indexed;
import com.vaadin.data.Item;

import guse.workflowrepresentation.GuseNode;
import guse.workflowrepresentation.GuseWorkflowRepresentation;
import guse.workflowrepresentation.InputPort;
import parsers.WFConfigWriter;
import submitter.parameters.Parameter;


public class WorkflowAdminPanelController implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = -1342968825888425959L;

  WFConfigWriter writer = new WFConfigWriter();
  private String pathToWorkflowConfig;

  public WorkflowAdminPanelController(String pathToWorkflowConfig) {
    this.pathToWorkflowConfig = pathToWorkflowConfig;
  }

  /**
   * returns a GuseWorkflowRepresentation, that contains only a subset of the parameters of the
   * "nativeWorkflow", selectedRows contains the choosen parameters.
   * 
   * @param selectedRows
   * @param container
   * @param nativeWorkflow
   * @param description
   * @param version
   * @param experimentType
   * @param sampleType
   * @return
   */
  public GuseWorkflowRepresentation showPreview(Collection<Object> selectedRows, Indexed container,
      GuseWorkflowRepresentation nativeWorkflow, String name, String description, String version,
      String experimentType, String sampleType) {
    LinkedHashMap<String, HashMap<String, ArrayList<Item>>> nodePortParam =
        new LinkedHashMap<String, HashMap<String, ArrayList<Item>>>();

    for (java.util.Iterator<Object> it = selectedRows.iterator(); it.hasNext() != false;) {
      Object o = it.next();
      Item item = container.getItem(o);
      String nodeName = (String) item.getItemProperty("node name").getValue();
      // String description = (String) item.getItemProperty("description").getValue();
      String paramname = (String) item.getItemProperty("name").getValue();
      // Object defaultvalue = (Object) item.getItemProperty("default").getValue();
      // Boolean isadvanced = (Boolean) item.getItemProperty("is advanced").getValue();
      // Boolean isrequired = (Boolean) item.getItemProperty("is required").getValue();
      String[] split = paramname.split(":");
      HashMap<String, ArrayList<Item>> portParam = null;
      if (nodePortParam.containsKey(nodeName)) {
        portParam = nodePortParam.get(nodeName);
        if (portParam.containsKey(split[0])) {
          portParam.get(split[0]).add(item);
        } else {
          ArrayList<Item> i = new ArrayList<Item>();
          i.add(item);
          portParam.put(split[0], i);
        }
      } else {
        portParam = new HashMap<String, ArrayList<Item>>();
        ArrayList<Item> i = new ArrayList<Item>();
        i.add(item);
        portParam.put(split[0], i);
        nodePortParam.put(nodeName, portParam);
      }
    }

    Map<String, GuseNode> newNodes = new HashMap<String, GuseNode>();
    for (String nodeName : nodePortParam.keySet()) {
      GuseNode nativeNode = nativeWorkflow.getNode(nodeName);
      GuseNode nodeClone = nativeNode.emptyClone();
      nodeClone.setMoabKeydirectives(nativeNode.getMoabKeydirectives());
      nodeClone.setCmdParams(nativeNode.getCmdParams());
      HashMap<String, ArrayList<Item>> portParam = nodePortParam.get(nodeName);
      Map<String, InputPort> inputPortsClone = new HashMap<String, InputPort>();
      for (String portName : portParam.keySet()) {
        System.out.println(portName);
        InputPort nativePort = nativeNode.getPort(portName);
        InputPort portClone = nativePort.emptyClone();
        Map<String, Parameter> paramsClone = new HashMap<String, Parameter>();
        for (Item item : portParam.get(portName)) {
          String paramname = (String) item.getItemProperty("name").getValue();
          Object defaultvalue = (Object) item.getItemProperty("default").getValue();
          // Boolean isadvanced = (Boolean) item.getItemProperty("is advanced").getValue();
          // Boolean isrequired = (Boolean) item.getItemProperty("is required").getValue();
          String[] split = paramname.split(":");
          String key = "";
          if (split.length == 2) {
            key = split[1];
          }
          Parameter param = nativePort.getParams().get(key);
          param.setValue(defaultvalue);
          // TODO set is Advanced and is required.

          paramsClone.put(key, param);
        }
        portClone.setParams(paramsClone);
        inputPortsClone.put(portName, portClone);
      }
      nodeClone.setInputPorts(inputPortsClone);
      newNodes.put(nodeName, nodeClone);
    }
    for (GuseNode node : nativeWorkflow.getGuseNodes()) {
      Map<String, InputPort> filestostage = node.getPortsByType(InputPort.Type.FILESTOSTAGE);
      Map<String, InputPort> jobnames = node.getPortsByType(InputPort.Type.JOBNAME);
      Map<String, InputPort> registernames = node.getPortsByType(InputPort.Type.REGISTERNAME);
      Map<String, InputPort> dropbox = node.getPortsByType(InputPort.Type.DROPBOX);
      Map<String, InputPort> user = node.getPortsByType(InputPort.Type.USER);
      if (newNodes.containsKey(node.getTitle())) {
        if (newNodes.get(node.getTitle()).getInputPorts() == null) {
          Map<String, InputPort> inputPorts = new HashMap<String, InputPort>();
          inputPorts.putAll(filestostage);
          inputPorts.putAll(jobnames);
          inputPorts.putAll(registernames);
          inputPorts.putAll(dropbox);
          inputPorts.putAll(user);
          newNodes.get(node.getTitle()).setInputPorts(inputPorts);
        } else {
          newNodes.get(node.getTitle()).getInputPorts().putAll(filestostage);
          newNodes.get(node.getTitle()).getInputPorts().putAll(jobnames);
          newNodes.get(node.getTitle()).getInputPorts().putAll(registernames);
          newNodes.get(node.getTitle()).getInputPorts().putAll(dropbox);
          newNodes.get(node.getTitle()).getInputPorts().putAll(user);
        }
      } else {
        GuseNode cloneNode = node.emptyClone();
        Map<String, InputPort> inputPorts = new HashMap<String, InputPort>();
        inputPorts.putAll(filestostage);
        inputPorts.putAll(jobnames);
        inputPorts.putAll(registernames);
        inputPorts.putAll(dropbox);
        inputPorts.putAll(user);
        cloneNode.setInputPorts(inputPorts);
        newNodes.put(cloneNode.getTitle(), cloneNode);

      }
    }

    GuseWorkflowRepresentation workflow = new GuseWorkflowRepresentation(nativeWorkflow.getID(),
        name, description, version, null, null, experimentType, sampleType);
    workflow.setNodes(newNodes);
    workflow.setDirectory(nativeWorkflow.getDirectory());
    return workflow;
  }

  public void saveWorkflowConfig() {

  }

  /**
   * Write a given workflow with given name as a configuration file to predefined
   * {@link: pathToWorkflowConfig} location.
   * 
   * @param workflow
   * @param configFileName
   * @throws JSONException
   * @throws IOException
   */
  public void writeWorkflowConfiguration(GuseWorkflowRepresentation workflow, String configFileName)
      throws JSONException, IOException {
    writer.write(configurationStringName(workflow.getID(), configFileName), workflow);
  }

  String configurationStringName(String workflowName, String configFileName) {
    StringBuilder sb = new StringBuilder(Paths.get(pathToWorkflowConfig, workflowName).toString());
    sb.append("__");
    sb.append(configFileName);
    if (!configFileName.endsWith(".config") && !configFileName.endsWith(".json")) {
      sb.append(".json");
    }
    return sb.toString();
  }



}
