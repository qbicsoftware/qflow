package life.qbic;

import com.vaadin.data.Item;
import com.vaadin.data.util.GeneratedPropertyContainer;
import com.vaadin.data.util.PropertyValueGenerator;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Grid;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.renderers.ButtonRenderer;
import com.vaadin.ui.renderers.ClickableRenderer;
import com.vaadin.ui.renderers.ClickableRenderer.RendererClickEvent;

import fasta.FastaBean;

public class FastaAdminPanel extends CustomComponent {

  /**
   * 
   */
  private static final long serialVersionUID = 4434876729350668424L;
  private FastaAdminPanelController controller;

  public FastaAdminPanel(FastaAdminPanelController controller) {
    this.setCaption("Fasta Admin Panel");
    TabSheet tabsheet = new TabSheet();
    this.controller = controller;
    controller.connect();
    for (int i = 0; i < controller.getTypesSize(); i++) {
      tabsheet.addComponent(initGridPanel(controller.getType(i)));
    }
    setCompositionRoot(tabsheet);
  }

  private Component initGridPanel(final FastaBean.Type type) {
    VerticalLayout mainLayout = new VerticalLayout();

    // Generate button caption column
    GeneratedPropertyContainer gpc = new GeneratedPropertyContainer(controller.getContainer(type));
    gpc.addGeneratedProperty("delete", new PropertyValueGenerator<String>() {

      @Override
      public String getValue(Item item, Object itemId, Object propertyId) {
        return "Delete"; // The caption
      }

      @Override
      public Class<String> getType() {
        return String.class;
      }
    });

    // Create a grid
    final Grid grid = new Grid(gpc);
    // Render a button that deletes the data row (item)
    grid.getColumn("delete")
        .setRenderer(new ButtonRenderer(new ClickableRenderer.RendererClickListener() {

          @Override
          public void click(RendererClickEvent event) {
            grid.getContainerDataSource().removeItem(event.getItemId());

          }

        }));
    mainLayout.addComponent(grid);


    Button save = new Button("save");
    save.addClickListener(new ClickListener() {
      @Override
      public void buttonClick(ClickEvent event) {
        controller.save(grid.getContainerDataSource(), type);
      }
    });
    mainLayout.addComponent(save);

    return mainLayout;
  }
}
