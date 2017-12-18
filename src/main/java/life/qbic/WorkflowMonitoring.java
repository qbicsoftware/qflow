package life.qbic;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Link;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.renderers.HtmlRenderer;
import com.vaadin.ui.renderers.ProgressBarRenderer;
import com.vaadin.ui.themes.ValoTheme;

import de.uni_tuebingen.qbic.beans.WorkflowMonitorBean;
import logging.Log4j2Logger;
import submitter.Submitter;

public class WorkflowMonitoring extends CustomComponent {


  /**
   * 
   */
  private static final long serialVersionUID = -4885815317306216637L;

  private logging.Logger LOGGER = new Log4j2Logger(WorkflowMonitoring.class);

  List<WorkflowMonitorBean> workflowMonitorBeans = new ArrayList<WorkflowMonitorBean>();

  Button refresh = new Button("refresh");
  private Grid workflowStatusTable = new Grid();

  enum status {
    running, starting, waiting, idle, failed, aborted, finished
  };

  Submitter submitter;

  private OpenbisControl openbisControl;

  private String user;

  private String qnavigatorUrl = null;


  public WorkflowMonitoring(Submitter submitter, OpenbisControl openbisControl, String user) {
    refresh.addStyleName(ValoTheme.BUTTON_FRIENDLY);
    refresh.setIcon(FontAwesome.REFRESH);
    this.submitter = submitter;
    this.openbisControl = openbisControl;
    this.user = user;
    VerticalLayout mainLayout = new VerticalLayout();
    setWidth(100, Unit.PERCENTAGE);

    mainLayout.setMargin(new MarginInfo(false, true, true, true));
    mainLayout.setWidth(100, Unit.PERCENTAGE);

    workflowStatusTable.setWidth(100, Unit.PERCENTAGE);

    mainLayout.addComponent(refresh);
    mainLayout.addComponent(workflowStatusTable);
    setControllers();
    setCompositionRoot(mainLayout);
  }

  private void setControllers() {
    refresh.addClickListener(new ClickListener() {

      /**
       * 
       */
      private static final long serialVersionUID = 1L;

      @Override
      public void buttonClick(ClickEvent event) {
        refresh();
      }
    });
  }

  public void setQnavigatorUrl(String qnavigatorUrl) {
    this.qnavigatorUrl = qnavigatorUrl;
  }

  public void refresh() {
    List<ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment> workflowIds =
        openbisControl.getWorkflowIDsForUser(user);
    BeanItemContainer<WorkflowMonitorBean> bic =
        new BeanItemContainer<WorkflowMonitorBean>(WorkflowMonitorBean.class);
    // try {
    for (ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment exp : workflowIds) {
      String qWfStatus = exp.getProperties().get("Q_WF_STATUS");
      String workflowName = exp.getProperties().get("Q_WF_NAME");
      String version = exp.getProperties().get("Q_WF_VERSION");
      String startedAt = exp.getProperties().get("Q_WF_STARTED_AT");
      String executedBy = exp.getProperties().get("Q_WF_EXECUTED_BY");

      double progress = 100.0;
      if (workflowName == null || workflowName.isEmpty()) {
        workflowName = openbisControl.openbis.openBIScodeToString(exp.getExperimentTypeCode());
      }
      if (version == null || version.isEmpty()) {
        version = "There can be only one! ";
      }

      if (qWfStatus == null || !(qWfStatus.equals("FAILED") || qWfStatus.equals("FINISHED"))) {
        try {
          qWfStatus = submitter.status(exp.getProperties().get("Q_WF_ID")).toString();
        } catch (ConnectException | NoSuchElementException e) {
          qWfStatus = "UNKNOWN";
          progress = 0.0;
        }
        progress = 0.5;
      }
      if (qnavigatorUrl == null || qnavigatorUrl.isEmpty()) {
        bic.addBean(new WorkflowMonitorBean(workflowName, qWfStatus, progress, version, executedBy,
            startedAt, exp.getIdentifier()));
      } else {
        String url = String.format("<a href='%s' target='_blank'>%s</a>",
            qnavigatorUrl + "#!experiment/" + exp.getIdentifier(), exp.getIdentifier());

        String caption = String.format("%s (%s)",
            openbisControl.openbis.openBIScodeToString(exp.getExperimentTypeCode()), exp.getCode());
        Link link = new Link(caption, new ExternalResource(url));

        WorkflowMonitorBean newBean = new WorkflowMonitorBean(workflowName, qWfStatus, progress,
            version, executedBy, startedAt, url);
        bic.addBean(newBean);
      }

    }
    // } catch (ConnectException | NoSuchElementException e) {
    // Notification.show("Workflow monitoring failed. " + e.getMessage(), Type.ERROR_MESSAGE);
    // }
    workflowStatusTable.setContainerDataSource(bic);
    workflowStatusTable.getColumn("progress").setRenderer(new ProgressBarRenderer());
    workflowStatusTable.getColumn("experiment").setRenderer(new HtmlRenderer());

  }
}
