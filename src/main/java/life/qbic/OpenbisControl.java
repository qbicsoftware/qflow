package life.qbic;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vaadin.data.Container;
import com.vaadin.data.util.BeanItemContainer;

import ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssDTO;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryTableModel;
import de.uni_tuebingen.qbic.beans.DatasetBean;
import life.qbic.openbis.openbisclient.OpenBisClient;

public class OpenbisControl {

  OpenBisClient openbis;

  Map<DatasetBean, ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet> dataMap;


  public OpenbisControl(OpenBisClient openbis) {
    this.openbis = openbis;
    dataMap = new HashMap<DatasetBean, ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet>();
  }

  public OpenBisClient getOpenbis() {
    return openbis;
  }

  public void setOpenbis(OpenBisClient openbis) {
    this.openbis = openbis;
  }

  protected Container fillTable2(
      List<ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet> datasets) {
    HashMap<String, DataSet> dataMap =
        new HashMap<String, ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet>();
    BeanItemContainer<DatasetBean> container =
        new BeanItemContainer<DatasetBean>(DatasetBean.class);
    Map<String, Object> params = new HashMap<String, Object>();
    List<String> dsCodes = new ArrayList<String>();

    for (ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet ds : datasets) {
      dsCodes.add(ds.getCode());
      dataMap.put(ds.getCode(), ds);
    }
    params.put("codes", dsCodes);
    QueryTableModel res = openbis.getAggregationService("query-files", params);
    for (Serializable[] ss : res.getRows()) {
      String dsCode = (String) ss[0];
      // when tryGetInternalPathInDataStore is used here for project like qmari it takes over a
      // minute. without 0.02s
      String path =
          /* dataMap.get(dsCode).getDataSetDss().tryGetInternalPathInDataStore() + */(String) ss[1];
      // path = path.replace("/mnt/DSS1", "/mnt/nfs/qbic");
      DatasetBean bean = new DatasetBean((String) ss[2], dataMap.get(dsCode).getDataSetTypeCode(),
          dsCode, path, dataMap.get(dsCode).getSampleIdentifierOrNull());
      container.addBean(bean);
    }


    return container;
  }


  protected Container fillTable(List<ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet> datasets) {
    BeanItemContainer<DatasetBean> container =
        new BeanItemContainer<DatasetBean>(DatasetBean.class);
    for (ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet d : datasets) {
      ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet dataSet =
          openbis.getFacade().getDataSet(d.getCode());
      FileInfoDssDTO[] filelist = dataSet.listFiles("original", false); // TODO recursive? only
                                                                        // original?
      for (FileInfoDssDTO info : filelist) {
        String fullPath = dataSet.getDataSetDss().tryGetInternalPathInDataStore() + "/original/"
            + info.getPathInListing();
        fullPath = fullPath.replace("/mnt/DSS1", "/mnt/nfs/qbic");
        DatasetBean bean = new DatasetBean(info.getPathInListing(), d.getDataSetTypeCode(),
            d.getCode(), fullPath, d.getSampleIdentifierOrNull());
        container.addBean(bean);
        dataMap.put(bean, dataSet);
      }
    }
    return container;
  }

  public String getPathInDSS(DatasetBean d) {
    // TODO mountpoints
    // qbis: /mnt/nfs/qbic
    // headnode: /mnt/DSS1
    String path = dataMap.get(d).getDataSetDss().tryGetInternalPathInDataStore() + "/original/"
        + d.getFileName();
    path.replace("/mnt/DSS1", "/mnt/nfs/qbic");
    return path;
  }

  /**
   * Register a new Workflow experiment in openBIS. Should be done when starting the workflow.
   * Experiment name is automatically created from the project name and the number of existing
   * experiments in that project. Standard workflow experiment fields are also initialized.
   * 
   * @param space space code
   * @param project project code
   * @param typecode openbis type code of the workflow
   * @param wfName name of the workflow
   * @param wfVersion version of the workflow
   * @param userID the user that starts the workflow
   * @return Code of the newly registered experiment
   */
  public String registerWFExperiment(String space, String project, String typecode, String wfName,
      String wfVersion, String userID) {
    int last = 0;
    for (Experiment e : openbis.getExperimentsOfProjectByIdentifier("/" + space + "/" + project)) {
      String[] codeSplit = e.getCode().split("E");
      String number = codeSplit[codeSplit.length - 1];
      int num = 0;
      try {
        num = Integer.parseInt(number);
      } catch (NumberFormatException ex) {
      }
      last = Math.max(num, last);
    }
    String code = project + "E" + Integer.toString(last + 1);
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("code", code);
    params.put("type", typecode);
    params.put("project", project);
    params.put("space", space);
    Map<String, Object> properties = new HashMap<String, Object>();
    properties.put("Q_WF_NAME", wfName);
    properties.put("Q_WF_VERSION", wfVersion);
    properties.put("Q_WF_EXECUTED_BY", userID);
    properties.put("Q_WF_STARTED_AT", getTime());
    properties.put("Q_WF_STATUS", "RUNNING");
    params.put("properties", properties);
    openbis.ingest("DSS1", "register-exp", params);
    return code;
  }

  public String registerWFSample(String space, String project, String experiment, String typecode,
      List<String> parents) {
    int last = 0;
    for (Sample s : openbis
        .getSamplesofExperiment("/" + space + "/" + project + "/" + experiment)) {
      String[] codeSplit = s.getCode().split("R");
      String number = codeSplit[codeSplit.length - 1];
      int num = 0;
      try {
        num = Integer.parseInt(number);
      } catch (NumberFormatException ex) {
      }
      last = Math.max(num, last);
    }

    String code = experiment + "R" + Integer.toString(last + 1);
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("code", code);
    params.put("type", typecode);

    params.put("sample_class", "");
    params.put("parents", parents);

    params.put("project", project);
    params.put("space", space);
    params.put("experiment", experiment);

    Map<String, Object> properties = new HashMap<String, Object>();
    // TODO fill properties
    params.put("properties", properties);

    openbis.ingest("DSS1", "register-samp", params);
    return code;
  }

  /**
   * Set the workflow ID for a workflow experiment. This must be the experiment whose code has been
   * given to the submitter to ensure correct registration of the results and log files.
   * 
   * @param space space code
   * @param project project code
   * @param experiment experiment code
   * @param wfID workflow ID created by the submitter for this workflow experiment
   */
  public void setWorkflowID(String space, String project, String experiment, String wfID) {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("identifier", "/" + space + "/" + project + "/" + experiment);
    Map<String, Object> properties = new HashMap<String, Object>();
    properties.put("Q_WF_ID", wfID);
    params.put("properties", properties);
    openbis.ingest("DSS1", "notify-user", params);
  }

  private Object getTime() {
    Date dNow = new Date();
    SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ZZZ");
    return ft.format(dNow);
  }

  public List<Experiment> getWorkflowIDsForUser(String userID) {
    List<Experiment> res = new ArrayList<>();
    for (Experiment e : openbis.getExperimentsForUser(userID)) {
      String wfID = e.getProperties().get("Q_WF_ID");
      if (wfID != null)
        res.add(e);
    }
    return res;
  }

  public List<Experiment> getRunningWorkflowIDsForUser(String userID) {
    List<Experiment> res = new ArrayList<>();
    for (Experiment e : openbis.getExperimentsForUser(userID)) {
      String wfID = e.getProperties().get("Q_WF_ID");
      if (wfID != null) {
        if (e.getProperties().get("Q_WF_FINISHED_AT") == null)
          res.add(e);
      }
    }
    return res;
  }

  public List<String> getConnectedSamples(List<DatasetBean> datasetBeans) {
    List<String> sampleIDs = new ArrayList<String>();

    for (DatasetBean bean : datasetBeans) {
      sampleIDs.add(bean.getSampleIdentifier());
    }

    return sampleIDs;
  }

}
