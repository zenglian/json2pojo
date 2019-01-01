package liwey.json2pojo;

import static liwey.json2pojo.ConfigUtil.config;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * A custom IntelliJ action which loads a dialog which will generate Java POJO classes from a given JSON text.
 */
public class GenerateAction extends AnAction {
  @Override
  public void actionPerformed(AnActionEvent event) {
    ConfigUtil.setLocale();

    // Get the action folder
    Project project = event.getProject();
    VirtualFile actionFolder = event.getData(LangDataKeys.VIRTUAL_FILE);

    if (project != null && actionFolder != null && actionFolder.isDirectory()) {
      // Get the module source root and effective package name
      VirtualFile moduleSourceRoot = ProjectRootManager.getInstance(project).getFileIndex().getSourceRootForFile(actionFolder);
      String packageName = ProjectRootManager.getInstance(project).getFileIndex().getPackageNameByDirectory(actionFolder);

      // Show JSON dialog
      GeneratorDialog dialog = new GeneratorDialog(packageName, moduleSourceRoot.getPath());
      dialog.setVisible(true);
    }
  }

  @Override
  public void update(AnActionEvent event) {
    // Get the project and action folder
    Project project = event.getProject();
    VirtualFile actionFolder = event.getData(LangDataKeys.VIRTUAL_FILE);

    if (project != null && actionFolder != null && actionFolder.isDirectory()) {
      // Set visibility based on if the package name is non-null
      String packageName = ProjectRootManager.getInstance(project).getFileIndex().getPackageNameByDirectory(actionFolder);
      event.getPresentation().setVisible(packageName != null);
    } else {
      event.getPresentation().setVisible(false);
    }
  }
}
