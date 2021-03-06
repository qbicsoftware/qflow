package life.qbic;

import java.io.IOException;
import java.util.HashMap;

import javax.portlet.PortletContext;
import javax.portlet.PortletSession;
import javax.servlet.http.HttpServletResponse;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.theme.ThemeDisplay;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Widgetset;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.server.WrappedPortletSession;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import life.qbic.logging.Log4j2Logger;
import life.qbic.logging.Logger;
import life.qbic.openbis.openbisclient.OpenBisClient;
import life.qbic.portal.liferayandvaadinhelpers.main.ConfigurationManager;
import life.qbic.portal.liferayandvaadinhelpers.main.ConfigurationManagerFactory;
import life.qbic.portal.liferayandvaadinhelpers.main.LiferayAndVaadinUtils;
import submitter.Submitter;

@Theme("mytheme")
@SuppressWarnings("serial")
@Widgetset("life.qbic.AppWidgetSet")
public class MyPortletUI extends UI {

  private Logger LOGGER = new Log4j2Logger(MyPortletUI.class);


  @Override
  protected void init(VaadinRequest request) {

    if (LiferayAndVaadinUtils.getUser() == null) {
      setContent(new Label("Please log in."));
      return;
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
              "openbis could not be accessed.");
        } catch (IOException | IllegalArgumentException e1) {
          // TODO Auto-generated catch block
          VaadinService.getCurrentResponse().setStatus(HttpServletResponse.SC_GATEWAY_TIMEOUT);
        }
      }

      return;
    }
    String qnavigatorUrl = null;
    ThemeDisplay themedisplay =
        (ThemeDisplay) VaadinService.getCurrentRequest().getAttribute(WebKeys.THEME_DISPLAY);
    try {
      String tmp = com.liferay.portal.util.PortalUtil.getLayoutFriendlyURL(themedisplay.getLayout(),
          themedisplay);
      int endIndex = tmp.lastIndexOf("/", tmp.length() - 1);
      qnavigatorUrl = tmp.substring(0, endIndex + 1);
      qnavigatorUrl = qnavigatorUrl.concat("qnavigator");
    } catch (PortalException | SystemException e) {
      LOGGER.error("some liferay features failed!!!", e.getStackTrace());
    }
    Submitter submitter = WorkflowSubmitterFactory.getSubmitter(
        WorkflowSubmitterFactory.Type.guseSubmitter, manager, new HashMap<String, String>());
    OpenbisControl c = new OpenbisControl(openbis);

    WorkflowMonitoring workflowMonitoring = new WorkflowMonitoring(submitter, c, userName);
    workflowMonitoring.setQnavigatorUrl(qnavigatorUrl);
    setContent(workflowMonitoring);
    workflowMonitoring.refresh();
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

  private String getPortletContextName(VaadinRequest request) {
    WrappedPortletSession wrappedPortletSession =
        (WrappedPortletSession) request.getWrappedSession();
    PortletSession portletSession = wrappedPortletSession.getPortletSession();

    final PortletContext context = portletSession.getPortletContext();
    final String portletContextName = context.getPortletContextName();
    return portletContextName;
  }

  private Integer getPortalCountOfRegisteredUsers() {
    Integer result = null;

    try {
      result = UserLocalServiceUtil.getUsersCount();
    } catch (SystemException e) {
      LOGGER.error("Error: " + e.toString());
    }

    return result;
  }
}
