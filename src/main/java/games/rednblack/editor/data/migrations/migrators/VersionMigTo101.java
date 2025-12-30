package games.rednblack.editor.data.migrations.migrators;

import games.rednblack.editor.data.migrations.IVersionMigrator;
import games.rednblack.editor.renderer.data.ProjectInfoVO;
import games.rednblack.editor.renderer.data.SceneVO;
import games.rednblack.h2d.common.vo.ProjectVO;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;

public class VersionMigTo101 implements IVersionMigrator {
    private String projectPath;
    private ProjectVO projectVO;
    private ProjectInfoVO projectInfoVO;

    @Override
    public void setProject(String path, ProjectVO vo, ProjectInfoVO projectInfoVO) {
        projectPath = path;
        projectVO = vo;
        this.projectInfoVO = projectInfoVO;
    }

    @Override
    public boolean doMigration() {
        for (SceneVO sceneVO : projectInfoVO.scenes) {
            File file = new File(projectPath + File.separator + "scenes" + File.separator + sceneVO.sceneName + ".dt");
            String target = "games.rednblack.editor.renderer.data.";
            String spine = "games.rednblack.h2d.extension.spine.";
            String talos = "games.rednblack.h2d.extension.talos.";
            String tinyvg = "games.rednblack.h2d.extension.tinyvg.";

            try {
                String content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
                String newContent = content.replace(target, "");
                newContent = newContent.replace(spine, "");
                newContent = newContent.replace(talos, "");
                newContent = newContent.replace(tinyvg, "");
                newContent = newContent.replaceAll("\"uniqueId\"\\s*:\\s*(\\d+)", "\"uniqueId\":\"$1\"");
                FileUtils.writeStringToFile(file, newContent, StandardCharsets.UTF_8);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }
}
