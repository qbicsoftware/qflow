package life.qbic;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Widgetset;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;


@Theme("mytheme")
@SuppressWarnings("serial")
@Widgetset("life.qbic.AppWidgetSet")
public class AboutUI extends UI {

  private String version = "0.1.2";
  private String revision = "568";

  @Override
  protected void init(VaadinRequest request) {
    VerticalLayout about = new VerticalLayout();
    about.addComponent(new Label("version: " + version));
    about.addComponent(new Label("revision: " + revision));
    about.addComponent(new Label(
        "Workflow execution. This apps can be used to submit and monitor workflows. Use it and give feedback! It still misses alot of functionality."));
    setContent(about);

  }
}
