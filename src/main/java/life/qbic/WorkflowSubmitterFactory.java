package life.qbic;

import java.io.File;
import java.util.Map;

import guse.impl.GuseSubmitter;
import guse.impl.GuseWorkflowFileSystem;
import guse.remoteapi.GuseRemoteApi;
import life.qbic.portal.liferayandvaadinhelpers.main.ConfigurationManager;
import submitter.Submitter;

public class WorkflowSubmitterFactory {

  public static enum Type {
    guseSubmitter, snakemakeSubmitter
  }

  public static Submitter getSubmitter(Type submitter, ConfigurationManager manager,
      Map<String, String> dropBoxPaths) {
    switch (submitter) {
      case guseSubmitter:
        GuseRemoteApi gra = new GuseRemoteApi();
        gra.setHost(manager.getGuseRemoteApiUrl());
        gra.setPASSWORD(manager.getGuseRemoteApiPass());

        GuseWorkflowFileSystem gwfs = null;
        try {
          gwfs = new GuseWorkflowFileSystem(new File(manager.getPathToGuseWorkflows()),
              new File(manager.getPathToGuseCertificate()), new File(manager.getPathToDropboxes()));
        } catch (Exception e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        return new GuseSubmitter(gra, gwfs, new File(manager.getPathToWFConfig()));
      case snakemakeSubmitter:
        return null;
      default:
        assert false;
        return null;
    }
  };
}
