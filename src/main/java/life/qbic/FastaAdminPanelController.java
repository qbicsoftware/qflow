package life.qbic;

import java.io.File;

import com.vaadin.data.Container.Indexed;
import com.vaadin.data.util.BeanItemContainer;

import fasta.FastaBean;

public class FastaAdminPanelController {
  FastaBean.Type[] types;
  private File directoryToFastaFile;
  private File[] allFiles;

  public FastaAdminPanelController(File directoryToFastaFile) {
    this.directoryToFastaFile = directoryToFastaFile;
    types = FastaBean.Type.values();
  }

  public boolean connect() {
    allFiles = directoryToFastaFile.listFiles();
    return (allFiles != null);
  }

  public int getTypesSize() {
    return types.length;
  }

  public FastaBean.Type getType(int i) {
    return types[i];
  }

  public void save(Indexed containerDataSource, FastaBean.Type type) {


  }

  public Indexed getContainer(FastaBean.Type type) {
    BeanItemContainer<FastaBean> container = new BeanItemContainer<FastaBean>(FastaBean.class);
    // TODO
    // should read json in and transform to container.
    // FastaDB

    return container;
  }

  public File getDirectoryToFastaFile() {
    return directoryToFastaFile;
  }

  public void setDirectoryToFastaFile(File directoryToFastaFile) {
    this.directoryToFastaFile = directoryToFastaFile;
  }

}
