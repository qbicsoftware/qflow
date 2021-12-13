package life.qbic;

import com.liferay.portal.kernel.exception.SystemException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import com.liferay.portal.model.Role;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Widgetset;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SampleType;
import guse.impl.GuseWorkflowFileSystem;
import life.qbic.logging.Log4j2Logger;
import life.qbic.logging.Logger;
import life.qbic.openbis.openbisclient.OpenBisClient;
import life.qbic.portal.liferayandvaadinhelpers.main.ConfigurationManager;
import life.qbic.portal.liferayandvaadinhelpers.main.ConfigurationManagerFactory;
import life.qbic.portal.liferayandvaadinhelpers.main.LiferayAndVaadinUtils;

@Theme("mytheme")
@SuppressWarnings("serial")
@Widgetset("life.qbic.AppWidgetSet")
public class AdminPanelUI extends UI {

  private static Logger LOGGER = new Log4j2Logger(AdminPanelUI.class);
  final String LIFERAY_ADMINISTRATOR = "Administrator";

  @Override
  protected void init(VaadinRequest request) {
    if (LiferayAndVaadinUtils.getUser() == null) {
      setContent(new Label("Please log in."));
      return;
    }


    try {
      if (!isAdmin()) {
        setContent(new Label("You don't have permissions to access this portlet."));
        return;
      }
    } catch (com.liferay.portal.kernel.exception.SystemException e2) {
      // TODO Auto-generated catch block
      e2.printStackTrace();
    }
    /*
     * String userName = LiferayAndVaadinUtils.getUser().getScreenName(); int height =
     * getPage().getBrowserWindowHeight(); int width = getPage().getBrowserWindowWidth(); WebBrowser
     * browser = getPage().getWebBrowser();
     */

    ConfigurationManager manager = ConfigurationManagerFactory.getInstance();

    final OpenBisClient openbis = new OpenBisClient(manager.getDataSourceUser(),
        manager.getDataSourcePassword(), manager.getDataSourceUrl());
    try {
      openbis.login();
    } catch (Exception e) {
      // probably the connection to openbis failed
      buildOpenbisConnectionErrorLayout(request);
      if (isInProductionMode()) {
        try {
          VaadinService.getCurrentResponse().sendError(HttpServletResponse.SC_GATEWAY_TIMEOUT,
              "openbis could not be accessed.");
        } catch (IOException | IllegalArgumentException e1) {
          // TODO Auto-generated catch block
          VaadinService.getCurrentResponse().setStatus(HttpServletResponse.SC_GATEWAY_TIMEOUT);
        }
      }

      return;
    }


    List<SampleType> samples = openbis.getFacade().listSampleTypes();
    List<String> sampleTypes = new ArrayList<String>();

    for (SampleType st : samples) {
      sampleTypes.add(st.getCode());
    }

    List<ExperimentType> experiments = openbis.getFacade().listExperimentTypes();
    List<String> experimentTypes = new ArrayList<String>();

    for (ExperimentType exp : experiments) {
      if (exp.getCode().contains("WF")) {
        experimentTypes.add(exp.getCode());
      }
    }

    /*
     * TODO "filedirectory" in a config files should only contain the directory name of the workflow
     * folder. just in case path_to_wf_config is different then the one in filedirectory so that we
     * can easily put a config file from one server to another if needed. UPDATE: Maybe it is better
     * to save the directory name as the id and just in case filedirectory is not found use
     * manager.getPathToGuseWorkflows + id.
     */
    WorkflowAdminPanelController workflowAdminController =
        new WorkflowAdminPanelController(manager.getPathToWFConfig());


    try {
      GuseWorkflowFileSystem gwfs =
          new GuseWorkflowFileSystem(new File(manager.getPathToGuseWorkflows()),
              new File(manager.getPathToGuseCertificate()), new File(manager.getPathToDropboxes()));

      WorkflowAdminPanel workflowAdminPanel = new WorkflowAdminPanel(gwfs.getWorkflows(),
          workflowAdminController, experimentTypes, sampleTypes);

      setContent(workflowAdminPanel);

    } catch (Exception e) {

      Notification
          .show("error while trying to get current workflows from repository:" + e.getMessage());
      e.printStackTrace();
    }



  }

  private boolean isAdmin() throws com.liferay.portal.kernel.exception.SystemException {
    boolean isAdmin = false;

    try {

      for (Role role : LiferayAndVaadinUtils.getUser().getRoles()) {
        if (role.getName().equals(LIFERAY_ADMINISTRATOR)) {
          isAdmin = true;
          break;
        }
      }
    } catch (SystemException e) {
      Notification.show(
          "System error. I dont know what that means, see the logs. Maybe there are more informations.");
      e.printStackTrace();
      return false;
    }
    return isAdmin;
  }

  /**
   * standard error layout, if connection to database failed.
   * 
   * @param request
   */
  private void buildOpenbisConnectionErrorLayout(final VaadinRequest request) {
    VerticalLayout vl = new VerticalLayout();
    this.setContent(vl);
    vl.addComponent(new Label(
        "An error occured, while trying to connect to the database. Please try again later, or contact your project manager."));
  }

  private boolean isInProductionMode() {
    return VaadinService.getCurrent().getDeploymentConfiguration().isProductionMode();
  }

}
