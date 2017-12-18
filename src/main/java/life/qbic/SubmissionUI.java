package life.qbic;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Widgetset;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.server.WebBrowser;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import life.qbic.openbis.openbisclient.OpenBisClient;
import life.qbic.portal.liferayandvaadinhelpers.main.ConfigurationManager;
import life.qbic.portal.liferayandvaadinhelpers.main.ConfigurationManagerFactory;
import life.qbic.portal.liferayandvaadinhelpers.main.LiferayAndVaadinUtils;
import logging.Log4j2Logger;
import submitter.Submitter;

@Theme("mytheme")
@SuppressWarnings("serial")
@Widgetset("life.qbic.AppWidgetSet")
public class SubmissionUI extends UI {
  private logging.Logger LOGGER = new Log4j2Logger(SubmissionUI.class);

  @Override
  protected void init(VaadinRequest request) {
    if (LiferayAndVaadinUtils.getUser() == null) {
      setContent(new Label("Please log in."));
      return;
    }
    if (!isInProductionMode()) {
      LOGGER.warn("portlet is not in production mode.");
    }
    String userName = LiferayAndVaadinUtils.getUser().getScreenName();
    ConfigurationManager manager = ConfigurationManagerFactory.getInstance();
    final OpenBisClient openbis = new OpenBisClient(manager.getDataSourceUser(),
        manager.getDataSourcePassword(), manager.getDataSourceUrl());

    initLayout();
    try {
      openbis.login();
    } catch (Exception e) {
      // probably the connection to openbis failed
      buildOpenbisConnectionErrorLayout(request);
      if (isInProductionMode()) {
        try {
          VaadinService.getCurrentResponse().sendError(HttpServletResponse.SC_GATEWAY_TIMEOUT,
              "An error occured, while trying to connect to the database. Please try again later, or contact your project manager.");
        } catch (IOException | IllegalArgumentException e1) {
          // TODO Auto-generated catch block
          VaadinService.getCurrentResponse().setStatus(HttpServletResponse.SC_GATEWAY_TIMEOUT);
        }
      }
      return;
    }

    int height = getPage().getBrowserWindowHeight();
    int width = getPage().getBrowserWindowWidth();
    WebBrowser browser = getPage().getWebBrowser();

    Submitter submitter = null;

    WFSubmissionView workflowSubmission = null;

    File dropboxConfigFile = new File(manager.getPathToDropboxes());
    try {
      submitter = WorkflowSubmitterFactory.getSubmitter(WorkflowSubmitterFactory.Type.guseSubmitter,
          manager, parseDropBoxPaths(dropboxConfigFile));
      OpenbisControl openbisControl = new OpenbisControl(openbis);
      workflowSubmission =
          new WFSubmissionView(submitter, openbisControl, userName, height, width, browser);

    } catch (Exception e) {
      // TODO Have proper exception handling
      e.printStackTrace();
    }
    if (workflowSubmission == null) {
      setContent(new Label("Error: Workflow Submission is currently not available"));
    } else {
      setContent(workflowSubmission);
    }


  }

  private Map<String, String> parseDropBoxPaths(File file) throws Exception {
    InputStream inputStream = new FileInputStream(file);
    byte[] buffer = new byte[inputStream.available()];
    while (inputStream.read(buffer) != -1);

    String jsonText = new String(buffer);
    JSONObject jsonObject = new JSONObject(jsonText);

    JSONObject jsonDropBox = jsonObject.getJSONObject("dropboxpaths");
    JSONArray jsonDropBoxPaths = jsonDropBox.getJSONArray("paths");

    // node map of workflow
    Map<String, String> dropBoxPaths = new HashMap<String, String>();

    // iterate over nodes of workflow
    for (int i = 0; i < jsonDropBoxPaths.length(); i++) {
      JSONObject jsonDropBoxPath = jsonDropBoxPaths.getJSONObject(i);

      dropBoxPaths.put(jsonDropBoxPath.getString("sampletype"),
          jsonDropBoxPath.getString("dropboxpath"));
    }

    inputStream.close();
    return dropBoxPaths;
  }

  private void initLayout() {
    HorizontalLayout layout = new HorizontalLayout();
    ProgressBar progress = new ProgressBar();
    progress.setIndeterminate(true);
    Label label = new Label("Connecting to database.");
    layout.addComponent(label);
    layout.addComponent(progress);
    setContent(layout);
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
