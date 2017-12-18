package life.qbic;

import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;


public class WorkflowLayout extends CustomComponent {
  /**
   * 
   */
  private static final long serialVersionUID = -8833669547679422830L;

  private TabSheet tabsheet;


  public WorkflowLayout() {
    tabsheet = new TabSheet();
    setCompositionRoot(tabsheet);
  }

  void updateLayout(int browserWidth, int browserHeight) {
    this.setWidth((browserWidth * 0.6f), Unit.PIXELS);
  }


  void addTab(Component component, String caption) {
    Tab tab = tabsheet.addTab(component, caption);
    tab.setClosable(false);

  }


}
